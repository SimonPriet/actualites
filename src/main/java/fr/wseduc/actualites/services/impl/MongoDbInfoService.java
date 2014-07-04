package fr.wseduc.actualites.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.actualites.model.InfoMode;
import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Either.Right;
import fr.wseduc.webutils.security.SecuredAction;

public class MongoDbInfoService extends AbstractService implements InfoService {

	protected Map<String, List<InfoState>> viewStatePermissions;
	
	public MongoDbInfoService(final String collection) {
		super(collection);
	}
	
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions, final Map<String, List<InfoState>> viewStatePermissions) {
		this.eb = vertx.eventBus();
		this.mongo = MongoDb.getInstance();
		this.notification = new TimelineHelper(vertx, eb, container);
		this.viewStatePermissions = viewStatePermissions;
	}

	@Override
	public void create(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Prepare Info object
		ObjectId newId = new ObjectId();
		JsonObject now = MongoDb.now();
		info.cleanPersistedObject();
		info.getBody().putString("_id", newId.toStringMongod())
			.putObject("owner", new JsonObject()
				.putString("userId", info.getUser().getUserId())
				.putString("displayName", info.getUser().getUsername()))
			.putObject("created", now).putObject("modified", now)
			.putNumber("status", InfoState.DRAFT.getId());
		
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId());
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("infos", info.getBody());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void retrieve(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Not implemented
		handler.handle(new Either.Left<String, JsonObject>("Not implemented"));
	}

	@Override
	public void update(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId())
				.put("infos").elemMatch(new BasicDBObject("_id", info.getInfoId()));
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info object update
		for (String attr: info.getBody().getFieldNames()) {
			if (! info.isProtectedField(attr)) {
				modifier.set("infos.$." + attr, info.getBody().getValue(attr));
			}
		}
		modifier.set("infos.$.modified", MongoDb.now());
		
		// Prepare Thread update
		modifier.set("modified", MongoDb.now());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void delete(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId())
				.put("infos").elemMatch(new BasicDBObject("_id", info.getInfoId()));
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info delete
		JsonObject infoMatcher = new JsonObject();
		modifier.pull("infos", infoMatcher.putString("_id", info.getInfoId()));
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}
	
	@Override
	public void changeState(final InfoResource info, final InfoState targetState, final Handler<Either<String, JsonObject>> handler) {
		// Query
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info object update
		modifier.set("infos.$.status", targetState.getId());
		modifier.set("infos.$.modified", MongoDb.now());
		
		// Prepare Thread update
		modifier.set("modified", MongoDb.now());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}
	
	@Override
	public void addComment(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Query
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare comment object
		info.getBody()
			.putString("author", info.getUser().getUserId())
			.putString("authorName", info.getUser().getUsername())
			.putObject("posted", MongoDb.now());
		modifier.push("infos.$.comments", info.getBody());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}
	
	@Override
	public void list(final ThreadResource thread, final Handler<Either<String, JsonArray>> handler) {
		// Start with Thread if present
		QueryBuilder query;
		if (thread.getThreadId() == null) {
			query = QueryBuilder.start();
		}
		else {
			query = QueryBuilder.start("_id").is(thread.getThreadId());
		}
		
		// Visibility Filter
		if (thread.getUser() != null) {
			prepareVisibilityFilteredQuery(query, thread.getUser(), thread.getVisibilityFilter());
		} else {
			preparePublicVisibleQuery(query);
		}
		
		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(collection, MongoQueryBuilder.build(query), sort, null, validResultsHandler(new Handler<Either<String, JsonArray>>(){
			@Override
			public void handle(Either<String, JsonArray> event) {
				if (event.isRight()) {
					// Post-process
					try {
						// State Filter
						final JsonArray filteredResults = new JsonArray();
						if (thread.getStateFilter() == null) {
							filterResultsByStatesPermissions(event.right().getValue(), filteredResults, thread, handler);
						}
						else {
							filterResultsByStateAndStatesPermissions(event.right().getValue(), filteredResults, thread, handler);
						}
						handler.handle(new Either.Right<String, JsonArray>(filteredResults));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonArray>("Malformed response"));
					}
					return;
				}
				handler.handle(event);
			}	
		}));
		
	}
	
	protected void prepareVisibilityFilteredQuery(final QueryBuilder query, final UserInfos user, final  VisibilityFilter visibilityFilter) {
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId()).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId).get());
		}
		switch (visibilityFilter) {
			case OWNER:
				query.put("infos").elemMatch(
						QueryBuilder.start("owner.userId").is(user.getUserId()).get()
				);
				break;
			case OWNER_AND_SHARED:
				query.or(
						QueryBuilder.start("infos").elemMatch(
								QueryBuilder.start("owner.userId").is(user.getUserId()).get()
						).get(),
						QueryBuilder.start("shared").elemMatch(
								new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
						).get());
				break;
			case SHARED:
				query.put("shared").elemMatch(
								new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get());
				break;
			case PROTECTED:
				query.put("visibility").is(VisibilityFilter.PROTECTED.name());
				break;
			case PUBLIC:
				query.put("visibility").is(VisibilityFilter.PUBLIC.name());
				break;
			default:
				query.or(
						QueryBuilder.start("visibility").is(VisibilityFilter.PUBLIC.name()).get(),
						QueryBuilder.start("visibility").is(VisibilityFilter.PROTECTED.name()).get(),
						QueryBuilder.start("infos").elemMatch(
								QueryBuilder.start("owner.userId").is(user.getUserId()).get()
						).get(),
						QueryBuilder.start("shared").elemMatch(
								new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
						).get());
				break;
		}
	}
	
	protected void filterResultsByStatesPermissions(final JsonArray results, final JsonArray filteredResults, final ThreadResource thread, final Handler<Either<String, JsonArray>> handler) {

		for (Object result : results.toList()) {
			JsonObject threadResult = (JsonObject) result;
			
			if (! threadResult.containsField("infos")) {
				continue;
			}
			
			if (thread.getUser().getUserId().equals(threadResult.getObject("owner").getString("userId"))) {
				filteredResults.toList().addAll(threadResult.getArray("infos").toList());
				continue;
			}
			
			for (Object sharedObject : threadResult.getArray("shared").toList()) {
				JsonObject shared = (JsonObject) sharedObject;
				
				if (thread.getUser().getProfilGroupsIds().contains(shared.getString("groupId"))
						|| thread.getUser().getUserId().equals(shared.getString("userId"))) {
					
					for (Entry<String, List<InfoState>> viewPermission : viewStatePermissions.entrySet()) {
						if (shared.getBoolean(viewPermission.getKey())) {
							
							for (Object infoObject : threadResult.getArray("infos").toList()) {
								JsonObject info = (JsonObject) infoObject;
								
								if (viewPermission.getValue().contains(InfoState.stateFromId(info.getInteger("status")))) {
									filteredResults.toList().add(info);
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected void filterResultsByStateAndStatesPermissions(final JsonArray results, final JsonArray filteredResults, final ThreadResource thread, final Handler<Either<String, JsonArray>> handler) {
		
	}
	
	protected void filterResultsByState(final JsonArray filteredResults, final JsonArray infos, final InfoState state) {
		if (state == null) {
			filteredResults.toList().addAll(infos.toList());
		}
		else {
			
		}
	}
	

	@Override
	public void canDoByState(final UserInfos user, final String threadId, final String infoId, final String sharedMethod, final InfoState state, final Handler<Boolean> handler) {
		final QueryBuilder query = QueryBuilder.start();
		prepareIsSharedQuery(query, user, threadId, sharedMethod);
		
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", infoId);
		infoMatch.put("status", state.getId());
		query.put("infos").elemMatch(infoMatch);
		
		executeCountQuery(MongoQueryBuilder.build(query), 1, handler);
	}
	
	@Override
	public void canDoMineByState(final UserInfos user, final String threadId, final String infoId, final String sharedMethod, final InfoState state, final Handler<Boolean> handler) {
		final QueryBuilder query = QueryBuilder.start();
		prepareIsSharedQuery(query, user, threadId, sharedMethod);
		
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", infoId);
		infoMatch.put("status", state.getId());
		infoMatch.put("owner.userId", user.getUserId());
		query.put("infos").elemMatch(infoMatch);
		
		executeCountQuery(MongoQueryBuilder.build(query), 1, handler);
	}
	
	@Override
	public void canDoByStatesAndModes(final UserInfos user, final String threadId, final String infoId, final String sharedMethod, final Map<InfoMode, InfoState> statesAndModes, final Handler<Boolean> handler) {
		final QueryBuilder query = QueryBuilder.start();
		prepareIsSharedQuery(query, user, threadId, sharedMethod);
		
		List<DBObject> ors = new ArrayList<DBObject>(statesAndModes.size());
		for(Entry<InfoMode, InfoState> entry : statesAndModes.entrySet()) {
			DBObject infoMatch = new BasicDBObject();
			infoMatch.put("_id", infoId);
			infoMatch.put("status", entry.getValue().getId());
			ors.add(QueryBuilder.start().and(
					QueryBuilder.start("mode").is(entry.getKey().getId()).get(),
					QueryBuilder.start("infos").elemMatch(infoMatch).get()
					).get());
		}
		query.or((DBObject[]) ors.toArray());
		
		executeCountQuery(MongoQueryBuilder.build(query), 1, handler);
	}

	
	protected void prepareIsSharedQuery(final QueryBuilder query, final UserInfos user, final String threadId, final String sharedMethod) {
		// ThreadId
		query.put("_id").is(threadId);
		
		// Permissions
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId())
				.put(sharedMethod).is(true).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId)
					.put(sharedMethod).is(true).get());
		}
		query.or(
				QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
				QueryBuilder.start("visibility").is(VisibilityFilter.PUBLIC.name()).get(),
				QueryBuilder.start("visibility").is(VisibilityFilter.PROTECTED.name()).get(),
				QueryBuilder.start("shared").elemMatch(
						new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()).get()
		);
	}
	
	protected void executeCountQuery(final JsonObject query, final int expectedCountResult, final Handler<Boolean> handler) {
		mongo.count(collection, query, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				JsonObject res = event.body();
				handler.handle(
						res != null &&
						"ok".equals(res.getString("status")) &&
						expectedCountResult == res.getInteger("count")
				);
			}
		});
	}
}
