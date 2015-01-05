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
				" WHERE t.id = ? " +
				" AND (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds) +
				" OR t.owner = ?) " +
				" GROUP BY t.id, u.username" +
				" ORDER BY t.modified DESC";
			values = new JsonArray(id).add(groupsAndUserIds).add(user.getUserId());
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
	public void getPublishSharedWithIds(String categoryId, UserInfos user,
			Handler<Either<String, JsonArray>> handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getSharedWithIds(String threadId, UserInfos user,
			Handler<Either<String, JsonArray>> handler) {
		// TODO Auto-generated method stub

	}

}
