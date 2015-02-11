package net.atos.entng.actualites.services.impl;

import static net.atos.entng.actualites.Actualites.MANAGE_RIGHT_ACTION;

import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.RepositoryEvents;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import fr.wseduc.webutils.Either;

public class ActualitesRepositoryEvents implements RepositoryEvents {

	private static final Logger log = LoggerFactory.getLogger(ActualitesRepositoryEvents.class);
	private final boolean shareOldGroupsToUsers;

	public ActualitesRepositoryEvents(boolean shareOldGroupsToUsers) {
		this.shareOldGroupsToUsers = shareOldGroupsToUsers;
	}

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale) {
		// TODO Implement exportResources
		log.warn("[ActualitesRepositoryEvents] exportResources is not implemented");
	}

	@Override
	public void deleteGroups(JsonArray groups) {
		if(groups != null && groups.size() > 0) {
			final JsonArray gIds = new JsonArray();
			for (Object o : groups) {
				if (!(o instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) o;
				gIds.add(j.getString("group"));
			}
			if (!shareOldGroupsToUsers) {
				if (gIds.size() > 0) {
					// Delete the groups. Cascade delete : delete from members, thread_shares and info_shares too
					Sql.getInstance().prepared("DELETE FROM actualites.groups WHERE id IN " + Sql.listPrepared(gIds.toArray())
							, gIds, SqlResult.validRowsResultHandler(new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							if (event.isRight()) {
								log.info("[ActualitesRepositoryEvents][deleteGroups]The groups and their shares are deleted");
							} else {
								log.error("[ActualitesRepositoryEvents][deleteGroups] Error deleting the groups and their shares. Message : " + event.left().getValue());
							}
						}
					}));
				}
			} else {
				// TODO Implement shareOldGroupsToUsers
				log.warn("[ActualitesRepositoryEvents][deleteGroups] Case (shareOldGroupsToUsers) for Event is not implemented");
			}
		} else {
			log.warn("[ActualitesRepositoryEvents][deleteGroups] groups is null or empty");
		}
	}

	@Override
	public void deleteUsers(JsonArray users) {
		// TODO : make the user anonymous
		if (users != null && users.size() > 0) {
			final JsonArray uIds = new JsonArray();
			for (Object u : users) {
				if (!(u instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) u;
				uIds.add(j.getString("id"));
			}
			SqlStatementsBuilder statementsBuilder = new SqlStatementsBuilder();
			// Remove all thread shares from thread_shares table
			statementsBuilder.prepared("DELETE FROM actualites.thread_shares WHERE member_id IN " + Sql.listPrepared(uIds.toArray()), uIds);
			// Remove all news shares from info_shares table
			statementsBuilder.prepared("DELETE FROM actualites.info_shares WHERE member_id IN " + Sql.listPrepared(uIds.toArray()), uIds);
			// Delete users (Set deleted = true in users table)
			statementsBuilder.prepared("UPDATE actualites.users SET deleted = true WHERE id IN " + Sql.listPrepared(uIds.toArray()), uIds);
			// Delete all threads where the owner is deleted and no manager rights shared on these resources
			// Cascade delete : the news that belong to these threads will be deleted too
			// thus, no need to delete news that do not have a manager because the thread owner is still there
			statementsBuilder.prepared("DELETE FROM actualites.thread WHERE id IN (" +
										  " SELECT DISTINCT t.id" +
										  " FROM actualites.thread AS t" +
										  " lEFT JOIN actualites.thread_shares AS ts ON t.id = ts.resource_id" +
										  " LEFT JOIN actualites.users AS u ON t.owner = u.id" +
										  " WHERE u.deleted = true" +
										  " GROUP BY t.id, ts.action" +
										  " HAVING count(ts.action) = 0 OR ts.action != ?" +
									  ")"
								  	  , new JsonArray().add(MANAGE_RIGHT_ACTION));
			Sql.getInstance().transaction(statementsBuilder.build(), SqlResult.validRowsResultHandler(new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						log.info("[ActualitesRepositoryEvents][cleanDataBase] The resources created by users are deleted");
					} else {
						log.error("[ActualitesRepositoryEvents][cleanDataBase] Error deleting the resources created by users. Message : " + event.left().getValue());
					}
				}
			}));
		} else {
			log.warn("[ActualitesRepositoryEvents][deleteUsers] users is empty");
		}
	}


}
