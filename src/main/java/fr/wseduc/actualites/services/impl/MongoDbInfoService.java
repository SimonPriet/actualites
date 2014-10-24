package fr.wseduc.actualites.services.impl;

import static fr.wseduc.actualites.Actualites.COLLECTION;
import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.TimeZone;

import org.bson.types.ObjectId;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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

public class MongoDbInfoService extends AbstractService implements InfoService {

	public MongoDbInfoService(final String collection) {
		super(collection);
	}

	@Override
	public void create(final InfoResource info, final Handler<Either<String, JsonObject>> handler) throws ParseException {
		// Prepare Info object
		final ObjectId newId = new ObjectId();
		JsonObject now = MongoDb.now();
		info.cleanPersistedObject();

		JsonObject body = info.getBody();
		body.putString("_id", newId.toString())
			.putObject("owner", new JsonObject()
				.putString("userId", info.getUser().getUserId())
				.putString("displayName", info.getUser().getUsername()))
			.putObject("created", now).putObject("modified", now)
			.putNumber("status", InfoState.DRAFT.getId());

		replaceDate(body, "publicationDate");
		replaceDate(body, "expirationDate");

		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId());
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("infos", body);

		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validActionResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					try {
						if (event.right().getValue().getNumber("number").intValue() == 1) {
							// Respond with created info Id
							JsonObject created = new JsonObject();
							created.putString("_id", newId.toString());
							handler.handle(new Either.Right<String, JsonObject>(created));
						}
						else {
							handler.handle(new Either.Left<String, JsonObject>("Thread not found"));
						}
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonObject>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(event);
				}
			}
		}));
	}

	private void replaceDate(JsonObject body, String dateFieldname) throws ParseException {
		long date = body.getLong(dateFieldname, 0L);
		if(date != 0L) {
			// Replace received date (unix timestamp in seconds) by a JsonObject
			// so that it will be stored as a date in MongoDB, e.g. as ISODate("2014-10-22T12:39:21.823Z")
			body.putElement(dateFieldname,
					new JsonObject().putNumber("$date",
							TimeUnit.MILLISECONDS.convert(date, TimeUnit.SECONDS)));
		}
	}

	@Override
	public void retrieve(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// Prepare Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId())
				.put("infos").elemMatch(new BasicDBObject("_id", info.getInfoId()));

		// Projection
		JsonObject idMatch = new JsonObject();
		idMatch.putString("_id", info.getInfoId());
		JsonObject elemMatch = new JsonObject();
		elemMatch.putObject("$elemMatch", idMatch);
		JsonObject projection = new JsonObject();
		projection.putObject("infos", elemMatch);

		mongo.findOne(collection,  MongoQueryBuilder.build(query), projection, validResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					try {
						// Extract info
						JsonObject thread = event.right().getValue();
						JsonArray infos = thread.getArray("infos");
						JsonObject extractedInfo = infos.get(0);
						handler.handle(new Either.Right<String, JsonObject>(extractedInfo));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonObject>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(event);
				}
			}
		}));
	}

	@Override
	public void update(final InfoResource info, final Handler<Either<String, JsonObject>> handler) throws ParseException {
		// Query
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId())
				.put("infos").elemMatch(new BasicDBObject("_id", info.getInfoId()));

		JsonObject body = info.getBody();
		replaceDate(body, "publicationDate");
		replaceDate(body, "expirationDate");

		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		// Prepare Info object update
		for (String attr: body.getFieldNames()) {
			if (! info.isProtectedField(attr)) {
				modifier.set("infos.$." + attr, body.getValue(attr));
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
		final ObjectId newId = new ObjectId();
		info.getBody()
			.putString("_id", newId.toString())
			.putString("author", info.getUser().getUserId())
			.putString("authorName", info.getUser().getUsername())
			.putObject("posted", MongoDb.now());
		modifier.push("infos.$.comments", info.getBody());

		// Execute query
		mongo.update(collection, MongoQueryBuilder.build(query), modifier.build(), validResultHandler(new Handler<Either<String, JsonObject>>(){
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					try {
						// Extract info
						JsonObject comment = new JsonObject();
						comment.putString("_id", newId.toString());
						handler.handle(new Either.Right<String, JsonObject>(comment));
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonObject>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(event);
				}
			}
		}));
	}

	@Override
	public void deleteComment(InfoResource info, String commentId, Handler<Either<String, JsonObject>> handler) {
		// Query
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", info.getInfoId());
		QueryBuilder query = QueryBuilder.start("_id").is(info.getThreadId()).put("infos").elemMatch(infoMatch);

		// Prepare comment delete
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		JsonObject commentMatcher = new JsonObject();
		modifier.pull("infos.$.comments", commentMatcher.putString("_id", commentId));

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
			prepareVisibilityFilteredQuery(query, thread.getUser(), VisibilityFilter.ALL);
		} else {
			preparePublicVisibleQuery(query);
		}

		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("infos", 1);

		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(collection, MongoQueryBuilder.build(query), sort, projection, validResultsHandler(handler));
	}

	@Override
	public void listLastPublishedInfos(final UserInfos user, final int resultSize, final Handler<Either<String, JsonObject>> handler) {

		final String currentDate = getCurrentDate();

		StringBuilder cmd = new StringBuilder();
		cmd.append("{ \"aggregate\" : \"").append(COLLECTION)
		.append("\", \"pipeline\" : [")

		// ShareAndOwner
		.append("{ \"$match\" : { \"$or\" : [")
			.append("{ \"owner.userId\" : \"").append(user.getUserId())
			.append("\"}, {\"shared\" : { \"$elemMatch\" : { ")
			.append(getSharedMatch(user))
		.append("}}} ]}},")

		.append("{ \"$unwind\": \"$infos\" },")

		.append("{ \"$match\" : {")
		.append("\"infos.status\":").append(InfoState.PUBLISHED.getId())
		.append(", \"$or\": [")
			.append("{\"infos.publicationDate\": { \"$lte\": ").append(currentDate).append("}, \"infos.expirationDate\": { \"$gt\": ").append(currentDate).append("}},")
			.append("{\"infos.publicationDate\": null, \"infos.expirationDate\": { \"$gt\": ").append(currentDate).append("}},")
			.append("{\"infos.publicationDate\": { \"$lte\": ").append(currentDate).append("}, \"infos.expirationDate\": null},")
			.append("{\"infos.publicationDate\": null, \"infos.expirationDate\": null}")
		.append("]}},")

		.append("{ \"$project\" : {")
			.append("\"infos._id\": 1,")
			.append("\"infos.title\": 1,")
			.append("\"title\": 1,")
			// Put max(publicationDate, modified) in new field "date"
			.append("\"infos.date\": { \"$cond\" : { ")
				.append(" \"if\" : { \"$gt\" : [ \"$infos.publicationDate\", \"$infos.modified\" ] }, ")
				.append(" \"then\" : \"$infos.publicationDate\",")
				.append(" \"else\" : \"$infos.modified\" } }")
		.append("}},")

		.append("{ \"$sort\" : { \"infos.date\": -1 } },")

		// TODO : the number of news must be a parameter of the webservice
		.append("{ \"$limit\" : ").append(resultSize).append("}")
		.append("]}");

		mongo.command(cmd.toString(), validResultHandler(handler));
	}

	private String getSharedMatch(final UserInfos user) {
		String sharedMethod = "fr-wseduc-actualites-controllers-ActualitesController|listThreadInfos";

		StringBuilder sb = new StringBuilder();
		sb.append(" \"$or\" : [")
			.append(" { \"userId\" : \"").append(user.getUserId())
			.append("\", \"").append(sharedMethod).append("\" : true");

		for (String gpId: user.getProfilGroupsIds()) {
			sb.append("}, { \"groupId\" : \"").append(gpId)
				.append("\", \"").append(sharedMethod).append("\" : true");
		}
		sb.append("}]");

		return sb.toString();
	}

	/**
	 * @return Current date as Json string. Example : {$date: "2014-10-23T10:28:52.000Z" }
	 */
	private String getCurrentDate() {
		DateFormat formatter = new SimpleDateFormat("'{\"$date\": \"'yyyy-MM-dd'T'HH:mm:ss'.000Z\" }'");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		return formatter.format(new Date());
	}

	@Override
	public void listForLinker(final ThreadResource thread, final Handler<Either<String, JsonArray>> handler) {
		QueryBuilder query = QueryBuilder.start();

		// Visibility Filter
		if (thread.getUser() != null) {
			prepareVisibilityFilteredQuery(query, thread.getUser(), VisibilityFilter.ALL);
		} else {
			preparePublicVisibleQuery(query);
		}

		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("created", 0)
			.putNumber("modified", 0)
			.putNumber("mode", 0)
			.putNumber("owner", 0)
			.putNumber("infos.content", 0)
			.putNumber("infos.created", 0)
			.putNumber("infos.modified", 0);

		JsonObject sort = new JsonObject().putNumber("infos.title", 1);
		mongo.find(collection, MongoQueryBuilder.build(query), sort, projection, validResultsHandler(handler));
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
	public void canDoSharedOrMineByState(final UserInfos user, final String threadId, final String infoId, final String sharedMethod, final InfoState state, final Handler<Boolean> handler) {
		final QueryBuilder query = QueryBuilder.start();

		// Shared
		final QueryBuilder sharedQuery = QueryBuilder.start();
		prepareIsSharedQuery(sharedQuery, user, threadId, sharedMethod);
		DBObject infoMatch = new BasicDBObject();
		infoMatch.put("_id", infoId);
		infoMatch.put("status", state.getId());
		sharedQuery.put("infos").elemMatch(infoMatch);

		// Mine
		final QueryBuilder mineQuery = QueryBuilder.start();
		DBObject mineMatch = new BasicDBObject();
		mineMatch.put("_id", infoId);
		mineMatch.put("status", state.getId());
		mineMatch.put("owner.userId", user.getUserId());
		mineQuery.put("infos").elemMatch(mineMatch);

		query.or(sharedQuery.get(), mineQuery.get());

		executeCountQuery(MongoQueryBuilder.build(query), 1, handler);
	}

	@Override
	public void canDoByStatesAndModes(final UserInfos user, final String threadId, final String infoId, final String sharedMethod, final Map<InfoMode, InfoState> statesAndModes, final Handler<Boolean> handler) {
		final QueryBuilder query = QueryBuilder.start();
		prepareIsSharedQuery(query, user, threadId, sharedMethod);

		DBObject[] orsArray = new DBObject[statesAndModes.size()];
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
		query.or(ors.toArray(orsArray));

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
