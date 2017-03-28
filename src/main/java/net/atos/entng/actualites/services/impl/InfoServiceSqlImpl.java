/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.actualites.services.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import fr.wseduc.webutils.http.Renders;
import net.atos.entng.actualites.Actualites;
import net.atos.entng.actualites.Actualites;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;
import net.atos.entng.actualites.services.InfoService;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import static org.entcore.common.sql.SqlResult.validRowsResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

public class InfoServiceSqlImpl implements InfoService {

	protected static final Logger log = LoggerFactory.getLogger(Renders.class);
	private static final String THREAD_PUBLISH = "net-atos-entng-actualites-controllers-InfoController|publish";
	private static final String RESOURCE_SHARED = "net-atos-entng-actualites-controllers-InfoController|getInfo";

	/**
	 * Format object to create a new revision
	 * @param id info id
	 * @param data object containing info
	 * @return new object containing revision values
	 */
	private JsonObject mapRevision(Long id, JsonObject data) {
		JsonObject o = data.copy();
		o.removeField("id");
		o.removeField("status");
		o.removeField("thread_id");
		if (o.containsField("expiration_date")) o.removeField("expiration_date");
		if (o.containsField("publication_date")) o.removeField("publication_date");
		if (o.containsField("is_headline")) o.removeField("is_headline");
		o.putNumber("info_id", id);
		return o;
	}


	@Override
	public void create(final JsonObject data, final UserInfos user, final String eventStatus, final Handler<Either<String, JsonObject>> handler) {
		String queryNewInfoId = "SELECT nextval('actualites.info_id_seq') as id";
		Sql.getInstance().raw(queryNewInfoId, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
			@Override
			public void handle(Either<String, JsonObject> event) {
				if (event.isRight()) {
					final Long infoId = event.right().getValue().getLong("id");
					SqlStatementsBuilder s = new SqlStatementsBuilder();

					String userQuery = "SELECT "+ Actualites.NEWS_SCHEMA + ".merge_users(?,?)";
					s.prepared(userQuery, new JsonArray().add(user.getUserId()).add(user.getUsername()));

					data.putString("owner", user.getUserId()).putNumber("id", infoId);
					s.insert(Actualites.NEWS_SCHEMA + "." + Actualites.INFO_TABLE, data, "id");

					JsonObject revision = mapRevision(infoId, data);
					revision.putString("event", eventStatus);
					s.insert(Actualites.NEWS_SCHEMA + "." + Actualites.INFO_REVISION_TABLE, revision, null);

					Sql.getInstance().transaction(s.build(), validUniqueResultHandler(1, handler));
				} else {
					log.error("Failure to call nextval('"+ Actualites.NEWS_SCHEMA +".info_id_seq') sequence");
					handler.handle(new Either.Left<String, JsonObject>("An error occured when creating new info"));
				}
			}
		}));
	}

	@Override
	public void update(String id, JsonObject data, UserInfos user, String eventStatus, Handler<Either<String, JsonObject>> handler) {
		SqlStatementsBuilder s = new SqlStatementsBuilder();

		String userQuery = "SELECT "+ Actualites.NEWS_SCHEMA + ".merge_users(?,?)";
		s.prepared(userQuery, new JsonArray().add(user.getUserId()).add(user.getUsername()));

		StringBuilder sb = new StringBuilder();
		JsonArray values = new JsonArray();
		for (String attr : data.getFieldNames()) {
			sb.append(attr);
			if (attr.contains("date")) {
				sb.append("= to_timestamp(?, 'YYYY-MM-DD hh24:mi:ss'),");
			} else {
				sb.append(" = ?, ");
			}
			values.add(data.getValue(attr));
		}
		String query = "UPDATE " + Actualites.NEWS_SCHEMA + "." + Actualites.INFO_TABLE +
						" SET " + sb.toString() + "modified = NOW() " +
						"WHERE id = ? " +
						"RETURNING id";

		s.prepared(query, values.add(Integer.parseInt(id)));

		JsonObject revision = mapRevision(Long.parseLong(id), data);
		revision.putString("owner", user.getUserId());
		revision.putString("event", eventStatus);
		s.insert(Actualites.NEWS_SCHEMA + "." + Actualites.INFO_REVISION_TABLE, revision, null);

		Sql.getInstance().transaction(s.build(), SqlResult.validUniqueResultHandler(1, handler));
	}

	@Override
	public void retrieve(String id, Handler<Either<String, JsonObject>> handler) {
			String query;
			JsonArray values = new JsonArray();
			query = "SELECT i.id as _id, i.title, i.content, i.status, i.publication_date, i.expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
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
				" GROUP BY i.id, u.username, t.id" +
				" ORDER BY i.modified DESC";
			values.add(Sql.parseId(id));
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseSharedUnique(handler));
	}
	
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
			query = "SELECT i.id as _id, i.title, i.content, i.status, i.publication_date, i.expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
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
			query = "SELECT i.id as _id, i.title, i.content, i.status, i.publication_date, i.expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
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
			query = "SELECT i.id as _id, i.title, i.content, i.status, i.publication_date, i.expiration_date, i.is_headline, i.thread_id, i.created, i.modified" +
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
			String query;
			JsonArray values = new JsonArray();
			List<String> groupsAndUserIds = new ArrayList<>();
			groupsAndUserIds.add(user.getUserId());
			if (user.getGroupsIds() != null) {
				groupsAndUserIds.addAll(user.getGroupsIds());
			}
			query = "SELECT i.id as _id, i.title, u.username," +
				" CASE WHEN i.publication_date > i.modified" +
					" THEN i.publication_date" +
					" ELSE i.modified" +
					" END as date" +
				", json_agg(row_to_json(row(ios.member_id, ios.action)::actualites.share_tuple)) as shared" +
				", array_to_json(array_agg(group_id)) as groups" +
				" FROM actualites.info AS i" +
				" LEFT JOIN actualites.users AS u ON i.owner = u.id" +
				" LEFT JOIN actualites.info_shares AS ios ON i.id = ios.resource_id" +
				" LEFT JOIN actualites.members AS m ON (ios.member_id = m.id AND m.group_id IS NOT NULL)" +
				" WHERE ((ios.member_id IN " + Sql.listPrepared(groupsAndUserIds.toArray()) + "AND ios.action = ?) OR i.owner = ?)" +
				" AND (i.status = 3" +
					" AND ((i.publication_date IS NULL OR i.publication_date <= NOW()) AND (i.expiration_date IS NULL OR i.expiration_date + interval '1 days' >= NOW())))" +
				" GROUP BY i.id, u.username" +
				" ORDER BY date DESC" +
				" LIMIT ?";

			for(String value : groupsAndUserIds){
				values.add(value);
			}
			values.add(RESOURCE_SHARED);
			values.add(user.getUserId());
			values.add(resultSize);
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void listForLinker(UserInfos user, Handler<Either<String, JsonArray>> handler) {
		if (user != null) {
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
					" AND ((i.publication_date IS NULL OR i.publication_date <= NOW()) AND (i.expiration_date IS NULL OR i.expiration_date + interval '1 days' >= NOW())))" +
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
			Sql.getInstance().prepared(query.toString(), values, SqlResult.parseShared(handler));
		}
	}

	@Override
	public void getSharedWithIds(String infoId, final Handler<Either<String, JsonArray>> handler) {
		this.retrieve(infoId, new Handler<Either<String, JsonObject>>() {
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

	@Override
	public void getOwnerInfo(String infoId, Handler<Either<String, JsonObject>> handler) {
		if (infoId != null && !infoId.isEmpty()) {
			String query = "SELECT info.owner FROM actualites." + Actualites.INFO_TABLE + " WHERE" +
					" id = ?;";

			Sql.getInstance().prepared(query, new JsonArray().addNumber(Long.parseLong(infoId)),
					SqlResult.validUniqueResultHandler(handler));
		}
	}

    @Override
    public void getRevisions(Long infoId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT info_revision.id as _id, created, title, content, owner as " +
                "user_id, event as eventName, username " +
                "FROM "+ Actualites.NEWS_SCHEMA +".info_revision " +
                "INNER JOIN "+ Actualites.NEWS_SCHEMA +".users on (info_revision.owner = users.id) " +
                "WHERE info_id = ? " +
                "ORDER BY created DESC;";
        Sql.getInstance().prepared(query, new JsonArray().addNumber(infoId),
                SqlResult.validResultHandler(handler));
    }

}
