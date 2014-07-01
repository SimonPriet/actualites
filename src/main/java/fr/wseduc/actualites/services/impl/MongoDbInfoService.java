package fr.wseduc.actualites.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;

public class MongoDbInfoService extends AbstractService implements InfoService {

	public MongoDbInfoService(final String collection) {
		super(collection);
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
			.putObject("created", now).putObject("modified", now);
		
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId());
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("infos", info.getBody());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void retrieve(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Query
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);
		
		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("infos", 1);
		
		// Execute Query
		mongo.findOne(collection,  MongoQueryBuilder.build(query), projection, validResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					// Post-process
					try {
						JsonObject info = new JsonObject(((JsonObject) event.right().getValue().getArray("infos").get(0)).toMap());
						info.putString("threadId", event.right().getValue().getString("_id"));
						handler.handle(new Either.Right<String, JsonObject>(info));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonObject>("Malformed response"));
					}
					return;
				}
				handler.handle(event);
			}	
		}));
	}

	@Override
	public void update(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Query
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);
		
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
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);
		
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info delete
		modifier.pull("infos", new BasicDBObject("_id", info.getInfoId()));
		
		// Prepare Thread update
		modifier.set("modified", MongoDb.now());
		
		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(handler));
	}

	@Override
	public void list(final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		list(user, null, null, handler);
	}
	
	@Override
	public void list(final ThreadResource thread, final Handler<Either<String, JsonArray>> handler) {
		list(thread.getUser(), thread.getThreadId(), thread.getStateFilter(), handler);
	}
	
	protected void list(final UserInfos user, final String threadId, final InfoState state, final Handler<Either<String, JsonArray>> handler) {
		// TODO IMPLEMENT list Infos by state
		// TODO real Case User
		QueryBuilder query;
		if (user != null) {
			List<DBObject> groups = new ArrayList<>();
			groups.add(QueryBuilder.start("userId").is(user.getUserId()).get());
			for (String gpId: user.getProfilGroupsIds()) {
				groups.add(QueryBuilder.start("groupId").is(gpId).get());
			}
			
			QueryBuilder subQuery = new QueryBuilder().or(
				QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
				QueryBuilder.start("shared").elemMatch(
						new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
				).get()
			);
			
			if (state == null) {
				query = subQuery;
			}
			else {
				query = new QueryBuilder().and(
					subQuery.get(),
					QueryBuilder.start("state").is(state.getId()).get()
				);
			}
		} else {	
			query = QueryBuilder.start("visibility").is(VisibilityFilter.PUBLIC.name());
		}
		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(collection, MongoQueryBuilder.build(query), sort, null, validResultsHandler(handler));
	}
	
	@Override
	public void changeState(final InfoResource info, final InfoState targetState, final Handler<Either<String, JsonObject>> handler) {
		// TODO IMPLEMENT changeState
	}
}
