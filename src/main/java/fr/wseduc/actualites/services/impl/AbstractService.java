package fr.wseduc.actualites.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;

public abstract class AbstractService {

	protected final String collection;	
	protected MongoDb mongo;
	
	public AbstractService(final String collection) {
		this.collection = collection;
		this.mongo = MongoDb.getInstance();
	}
	
	protected void prepareVisibilityFilteredQuery(final QueryBuilder query, final UserInfos user, final  VisibilityFilter visibilityFilter) {
		List<DBObject> groups = new ArrayList<>();
		groups.add(QueryBuilder.start("userId").is(user.getUserId()).get());
		for (String gpId: user.getProfilGroupsIds()) {
			groups.add(QueryBuilder.start("groupId").is(gpId).get());
		}
		switch (visibilityFilter) {
			case PUBLIC:
				query.put("visibility").is(VisibilityFilter.PUBLIC.name());
				break;
			default:
				query.or(
						QueryBuilder.start("owner.userId").is(user.getUserId()).get(),
						QueryBuilder.start("shared").elemMatch(
								new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()
						).get());
				break;
		}
	}
	
	protected void preparePublicVisibleQuery(final QueryBuilder query) {
		query.put("visibility").is(VisibilityFilter.PUBLIC.name());
	}
}
