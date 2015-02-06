package net.atos.entng.actualites.services.impl;

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

	private static final String ANONYMOUS_PERSONNEL_ID = "1";
	private static final String ANONYMOUS_TEACHER_ID = "2";
	private static final String ANONYMOUS_STUDENT_ID = "3";
	private static final String ANONYMOUS_RELATIVE_ID = "4";
	private static final String MANAGE_RIGHT_ACTION = "net-atos-entng-actualites-controllers-ThreadController|updateThread";

	public ActualitesRepositoryEvents(boolean shareOldGroupsToUsers) {
		this.shareOldGroupsToUsers = shareOldGroupsToUsers;
	}

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale) {

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
								log.info("The groups " + gIds.toList().toString() + " and their shares are deleted");
							} else {
								log.error("[ActualitesRepositoryEvents][deleteGroups] Error deleting these groups " + gIds.toList().toString()
										+ " and their shares. Message : " + event.left().getValue());
							}
						}
					}));
				}
			} else {
				// TODO Implement shareOldGroupsToUsers
				log.error("[ActualitesRepositoryEvents][deleteGroups] Case (shareOldGroupsToUsers) for Event is not implemented");
			}
		} else {
			log.error("[ActualitesRepositoryEvents][deleteGroups] groups is empty");
		}
	}

	@Override
	public void deleteUsers(JsonArray users) {
		if (users != null && users.size() > 0) {
			final JsonArray personnelIds = new JsonArray();
			final JsonArray teacherIds = new JsonArray();
			final JsonArray studentIds = new JsonArray();
			final JsonArray relativeIds = new JsonArray();
			// Divide users ids by profiles
			for (Object u : users) {
				if (!(u instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) u;
				if("Personnel".equals(j.getString("type"))) {
					personnelIds.add(j.getString("id"));
				} else {
					if ("Teacher".equals(j.getString("type"))) {
						teacherIds.add(j.getString("id"));
					} else {
						if ("Student".equals(j.getString("type"))) {
							studentIds.add(j.getString("id"));
						} else {
							if ("Relative".equals(j.getString("type"))) {
								relativeIds.add(j.getString("id"));
							}
						}
					}
				}
			}
			// Clean the database after the users are artificially deleted.
			if (personnelIds.size() > 0) {
				this.cleanDataBase(personnelIds, ANONYMOUS_PERSONNEL_ID);
			}
			if (teacherIds.size() > 0) {
				this.cleanDataBase(teacherIds, ANONYMOUS_TEACHER_ID);
			}
			if (studentIds.size() > 0) {
				this.cleanDataBase(studentIds, ANONYMOUS_STUDENT_ID);
			}
			if (relativeIds.size() > 0) {
				this.cleanDataBase(relativeIds, ANONYMOUS_RELATIVE_ID);
			}
		} else {
			log.error("[ActualitesRepositoryEvents][deleteUsers] users is empty");
		}
	}

	private void cleanDataBase(final JsonArray usersIds, String anonymousId) {
		if (usersIds != null && usersIds.size() > 0) {
			SqlStatementsBuilder statementsBuilder = new SqlStatementsBuilder();
			// Remove all thread shares from thread_shares table
			statementsBuilder.prepared("DELETE FROM actualites.thread_shares WHERE member_id IN " + Sql.listPrepared(usersIds.toArray()), usersIds);
			// Remove all news shares from info_shares table
			statementsBuilder.prepared("DELETE FROM actualites.info_shares WHERE member_id IN " + Sql.listPrepared(usersIds.toArray()), usersIds);
			// Delete users (Set deleted = true in users table)
			statementsBuilder.prepared("UPDATE actualites.users SET deleted = true WHERE id IN " + Sql.listPrepared(usersIds.toArray()), usersIds);
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
								  	  , new JsonArray(MANAGE_RIGHT_ACTION));
			Sql.getInstance().transaction(statementsBuilder.build(), SqlResult.validRowsResultHandler(new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						log.info("The resources created by these users " + usersIds.toList().toString() + " are deleted");
					} else {
						log.error("Error deleting the resources created by these users " + usersIds.toList().toString() + ". Message : " + event.left().getValue());
					}
				}
			}));
		} else {
			log.error("[ActualitesRepositoryEvents][anonymizeOwner] usersIds is empty");
		}
	}

}
