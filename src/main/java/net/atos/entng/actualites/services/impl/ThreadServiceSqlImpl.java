package net.atos.entng.actualites.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;
import net.atos.entng.actualites.services.ThreadService;

public class ThreadServiceSqlImpl implements ThreadService {

	@Override
	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		String query;
		JsonArray values = new JsonArray();
		if (id != null && user != null) {
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT t.id as _id, t.title, t.icon, t.mode, t.created, t.modified, t.owner, u.username" +
				", json_agg(row_to_json(row(ts.member_id, ts.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.thread AS t" +
				" LEFT JOIN actualites.users AS u ON t.owner = u.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.members AS m ON (ts.member_id = m.id AND m.group_id IS NOT NULL)" +
				" WHERE t.id = ? " +
				" AND (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) +
				" OR t.owner = ?) " +
				" GROUP BY t.id, u.username" +
				" ORDER BY t.modified DESC";
			values.add(Sql.parseId(id));
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseSharedUnique(handler));
		}
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		String query;
		JsonArray values = new JsonArray();
		if (user != null) {
			List<String> gu = new ArrayList<>();
			gu.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				gu.addAll(user.getGroupsIds());
			}
			final Object[] groupsAndUserIds = gu.toArray();
			query = "SELECT t.id as _id, t.title, t.icon, t.mode, t.created, t.modified, t.owner, u.username" +
				", json_agg(row_to_json(row(ts.member_id, ts.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.thread AS t" +
				" LEFT JOIN actualites.users AS u ON t.owner = u.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.members AS m ON (ts.member_id = m.id AND m.group_id IS NOT NULL)" +
				" WHERE ts.member_id IN " + Sql.listPrepared(groupsAndUserIds) +
				" OR t.owner = ? " +
				" GROUP BY t.id, u.username" +
				" ORDER BY t.modified DESC";
			values = new JsonArray(groupsAndUserIds).add(user.getUserId());
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
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
							JsonObject owner = new JsonObject();
							owner.putString("userId", thread.getString("owner"));
							sharedWithIds.add(owner);
						}
						if (thread.containsField("shared")) {
							JsonArray shared = thread.getArray("shared");
							for(Object jo : shared){
								if(((JsonObject) jo).containsField("net-atos-entng-actualites-controllers-InfoController|publish")){
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

}
