package fr.wseduc.actualites.services.impl;

import static org.entcore.common.mongodb.MongoDbResult.*;

import java.util.ArrayList;
import java.util.List;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.actualites.services.InfoService;
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
		projection.putNumber("title", 1);
		projection.putNumber("icon", 1);
		projection.putNumber("order", 1);
		projection.putNumber("owner", 1);
		projection.putNumber("mode", 1);
		projection.putNumber("shared", 1);
		projection.putNumber("visibilty", 1);
		
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
		projection.putNumber("title", 1);
		projection.putNumber("icon", 1);
		projection.putNumber("order", 1);
		projection.putNumber("owner", 1);
		projection.putNumber("mode", 1);
		projection.putNumber("shared", 1);
		projection.putNumber("visibilty", 1);
		
		mongo.findOne(collection,  MongoQueryBuilder.build(builder), projection, validResultHandler(handler));
	}
	
	protected void prepareVisibilityFilteredQuery(final QueryBuilder query, final UserInfos user, final  VisibilityFilter visibilityFilter) {
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId()).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId).get());
		}
		switch (visibilityFilter) {
			case OWNER:
				query.put("owner.userId").is(user.getUserId());
				break;
			case OWNER_AND_SHARED:
				query.or(
						QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
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
						QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
						QueryBuilder.start("shared").elemMatch(
								new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
						).get());
				break;
		}
	}
}
