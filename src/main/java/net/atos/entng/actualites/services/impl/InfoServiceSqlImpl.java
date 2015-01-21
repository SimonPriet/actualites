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
import net.atos.entng.actualites.services.InfoService;

public class InfoServiceSqlImpl implements InfoService {

	private static final String THREAD_PUBLISH = "net-atos-entng-actualites-controllers-InfoController|publish";

	@Override
	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler) {
		if (user != null) {
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, i.content, i.status, to_timestamp(i.publication_date) AT TIME ZONE 'UTC-1' AS publication_date" +
				", to_timestamp(i.expiration_date) AT TIME ZONE 'UTC-1' AS expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
				", i.owner, u.username, t.title AS thread_title, t.icon AS thread_icon" +
				", (SELECT json_agg(cr.*) FROM (" +
					"SELECT c.id as _id, c.comment, c.owner, c.created, c.modified, au.username" +
					" FROM actualites.comment AS c" +
					" LEFT JOIN actualites.users AS au ON c.owner = au.id" +
					" WHERE i.id = c.info_id" +
					" ORDER BY c.modified ASC) cr)" +
					" AS comments" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON ((ts.member_id = m.id OR ios.member_id = m.id) AND m.group_id IS NOT NULL)" +
				" WHERE i.id = ? " +
				" AND ((i.owner = ? OR (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND i.status > 2))" +
				" OR ((t.owner = ? OR (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ts.action = ?)) AND i.status > 1))" +
				" GROUP BY i.id, u.username, t.id" +
				" ORDER BY i.modified DESC";
			values.add(Sql.parseId(id));
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(THREAD_PUBLISH);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseSharedUnique(handler));
		}
	}

	@Override
	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		if (user != null) {
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, i.content, i.status, to_timestamp(i.publication_date) AT TIME ZONE 'UTC-1' AS publication_date" +
				", to_timestamp(i.expiration_date) AT TIME ZONE 'UTC-1' AS expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
				", i.owner, u.username, t.title AS thread_title, t.icon AS thread_icon" +
				", (SELECT json_agg(cr.*) FROM (" +
					"SELECT c.id as _id, c.comment, c.owner, c.created, c.modified, au.username" +
					" FROM actualites.comment AS c" +
					" LEFT JOIN actualites.users AS au ON c.owner = au.id" +
					" WHERE i.id = c.info_id" +
					" ORDER BY c.modified ASC) cr)" +
					" AS comments" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON ((ts.member_id = m.id OR ios.member_id = m.id) AND m.group_id IS NOT NULL)" +
				" WHERE ((i.owner = ? OR (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND i.status > 2))" +
				" OR ((t.owner = ? OR (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ts.action = ?)) AND i.status > 1))" +
				" GROUP BY i.id, u.username, t.id" +
				" ORDER BY i.modified DESC";
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(THREAD_PUBLISH);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void listByThreadId(String id, UserInfos user, Handler<Either<String, JsonArray>> handler) {
		if (user != null) {
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, i.content, i.status, to_timestamp(i.publication_date) AT TIME ZONE 'UTC-1' AS publication_date" +
				", to_timestamp(i.expiration_date) AT TIME ZONE 'UTC-1' AS expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
				", i.owner, u.username, t.title AS thread_title, t.icon AS thread_icon" +
				", (SELECT json_agg(cr.*) FROM (" +
					"SELECT c.id as _id, c.comment, c.owner, c.created, c.modified, au.username" +
					" FROM actualites.comment AS c" +
					" LEFT JOIN actualites.users AS au ON c.owner = au.id" +
					" WHERE i.id = c.info_id" +
					" ORDER BY c.modified ASC) cr)" +
					" AS comments" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON ((ts.member_id = m.id OR ios.member_id = m.id) AND m.group_id IS NOT NULL)" +
				" WHERE t.id = ? " +
				" AND ((i.owner = ? OR (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND i.status > 2))" +
				" OR ((t.owner = ? OR (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ts.action = ?)) AND i.status > 1))" +
				" GROUP BY t.id, i.id, u.username" +
				" ORDER BY i.modified DESC";
			values.add(Sql.parseId(id));
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(THREAD_PUBLISH);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void listLastPublishedInfos(UserInfos user, int resultSize, Handler<Either<String, JsonArray>> handler) {
		if (user != null) {
			long unixTime = System.currentTimeMillis() / 1000L;
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, u.username, t.id AS thread_id, t.title AS thread_title," +
				" CASE WHEN to_timestamp(i.publication_date) AT TIME ZONE 'UTC-1' > i.modified" +
					" THEN to_timestamp(i.publication_date) AT TIME ZONE 'UTC-1'" +
					" ELSE i.modified" +
					" END as date" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON ((ts.member_id = m.id OR ios.member_id = m.id) AND m.group_id IS NOT NULL)" +
				" WHERE (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " OR i.owner = ?" +
					" OR (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ts.action = ?) OR t.owner = ?)" +
				" AND (i.status = 3" +
					" AND ((i.publication_date = NULL OR i.publication_date <= ?) AND (i.expiration_date = NULL OR i.expiration_date >= ?)))" +
				" GROUP BY i.id, u.username, t.id" +
				" ORDER BY date DESC" +
				" LIMIT ?";
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(THREAD_PUBLISH);
			values.add(user.getUserId());
			values.add(unixTime);
			values.add(unixTime);
			values.add(resultSize);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void listForLinker(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		if (user != null) {
			long unixTime = System.currentTimeMillis() / 1000L;
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, i.thread_id, i.owner, u.username, t.title AS thread_title, t.icon AS thread_icon" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.thread AS t ON i.thread_id = t.id" +
				" LEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON ((ts.member_id = m.id OR ios.member_id = m.id) AND m.group_id IS NOT NULL)" +
				" WHERE (ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " OR i.owner = ?" +
					" OR (ts.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + " AND ts.action = ?) OR t.owner = ?)" +
				" AND (i.status = 3" +
					" AND ((i.publication_date = NULL OR i.publication_date <= ?) AND (i.expiration_date = NULL OR i.expiration_date >= ?)))" +
				" GROUP BY i.id, u.username, t.id" +
				" ORDER BY i.title";
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(user.getUserId());
			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(THREAD_PUBLISH);
			values.add(user.getUserId());
			values.add(unixTime);
			values.add(unixTime);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void getSharedWithIds(String infoId, UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		this.retrieve(infoId, user, new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				JsonArray sharedWithIds = new JsonArray();
				if (event.isRight()) {
					try {
						JsonObject info = event.right().getValue();
						if (info.containsField("owner")) {
							JsonObject owner = new JsonObject();
							owner.putString("userId", info.getString("owner"));
							sharedWithIds.add(owner);
						}
						if (info.containsField("shared")) {
							JsonArray shared = info.getArray("shared");
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
