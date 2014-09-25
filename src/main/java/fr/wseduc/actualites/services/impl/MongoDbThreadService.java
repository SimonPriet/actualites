package fr.wseduc.actualites.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultsHandler;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.QueryBuilder;

import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

public class MongoDbThreadService extends AbstractService implements ThreadService {

	public MongoDbThreadService(final String collection) {
		super(collection);
	}

	@Override
	public void list(final VisibilityFilter visibilityFilter, final UserInfos user, Handler<Either<String, JsonArray>> handler) {
		// Start with Thread if present
		QueryBuilder query = QueryBuilder.start();

		// Visibility Filter
		if (user != null) {
			prepareVisibilityFilteredQuery(query, user, visibilityFilter);
		} else {
			preparePublicVisibleQuery(query);
		}

		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("infos", 0);

		JsonObject sort = new JsonObject().putNumber("modified", -1);
		mongo.find(collection, MongoQueryBuilder.build(query), sort, projection, validResultsHandler(handler));
	}

	@Override
	public void retrieve(String id, final UserInfos user, Handler<Either<String, JsonObject>> handler) {
		// Query
		QueryBuilder builder = QueryBuilder.start("_id").is(id);
		if (user == null) {
			builder.put("visibility").is(VisibilityFilter.PUBLIC.name());
		}

		// Projection
		JsonObject projection = new JsonObject();
		projection.putNumber("infos", 0);

		mongo.findOne(collection,  MongoQueryBuilder.build(builder), projection, validResultHandler(handler));
	}

	@Override
	public void getPublishSharedWithIds(String threadId, UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		this.retrieve(threadId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				JsonArray sharedWithIds = new JsonArray();
				if (event.isRight()) {
					try {
						JsonObject thread = event.right().getValue();
						if (thread.containsField("owner")) {
							sharedWithIds.add(thread.getObject("owner"));
						}
						if (thread.containsField("shared")) {
							JsonArray shared = thread.getArray("shared");
							for(Object jo : shared){
								if(((JsonObject) jo).containsField("fr-wseduc-actualites-controllers-ActualitesController|publish")){
									sharedWithIds.add(jo);
								}
							}
							handler.handle(new Either.Right<String, JsonArray>(sharedWithIds));
						}
						else {
							handler.handle(new Either.Right<String, JsonArray>(new JsonArray()));
						}
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonArray>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
				}
			}
		});
	}

	@Override
	public void getSharedWithIds(String threadId, UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		this.retrieve(threadId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				JsonArray sharedWithIds = new JsonArray();
				if (event.isRight()) {
					try {
						JsonObject thread = event.right().getValue();
						if (thread.containsField("owner")) {
							sharedWithIds.add(thread.getObject("owner"));
						}
						if (thread.containsField("shared")) {
							JsonArray shared = thread.getArray("shared");
							for(Object jo : shared){
								sharedWithIds.add(jo);
							}
							handler.handle(new Either.Right<String, JsonArray>(sharedWithIds));
						}
						else {
							handler.handle(new Either.Right<String, JsonArray>(new JsonArray()));
						}
					}
					catch (Exception e) {
						handler.handle(new Either.Left<String, JsonArray>("Malformed response : " + e.getClass().getName() + " : " + e.getMessage()));
					}
				}
				else {
					handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
				}
			}
		});
	}
}
