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

	public ActualitesRepositoryEvents(boolean shareOldGroupsToUsers) {
		this.shareOldGroupsToUsers = shareOldGroupsToUsers;
	}

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale) {

	}

	@Override
	public void deleteGroups(JsonArray groups) {
		if (shareOldGroupsToUsers && groups != null && groups.size() > 0){
			final JsonArray gIds = new JsonArray();
			for (Object o : groups) {
				if (!(o instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) o;
				gIds.add(j.getString("group"));
			}
			if (gIds.size() > 0) {
				String query = "DELETE FROM actualites.thread_shares WHERE member_id IN " + Sql.listPrepared(gIds.toArray());
				Sql.getInstance().prepared(query, gIds, SqlResult.validRowsResultHandler(new Handler<Either<String, JsonObject>>() {
					@Override
					public void handle(Either<String, JsonObject> event) {
						if (event.isRight()) {
							log.info("The groups " + gIds.toList().toString() + " are deleted");
						} else {
							log.error("Error deleting these groups " + gIds.toList().toString() + ". Message : " + event.left().getValue());
						}
					}
				}));
			}
		}
	}

	@Override
	public void deleteUsers(JsonArray users) {
		if (users != null && users.size() > 0){
			final JsonArray personnelIds = new JsonArray();
			final JsonArray teacherIds = new JsonArray();
			final JsonArray studentIds = new JsonArray();
			final JsonArray relativeIds = new JsonArray();
			for (Object u : users) {
				if (!(u instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) u;
				if("Personnel".equals(j.getString("type"))){
					personnelIds.add(j.getString("id"));
				} else {
					if ("Teacher".equals(j.getString("type"))){
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

			// For each profile, replace the owner id with the the an anonymous id of the profile.
			if (personnelIds.size() > 0){
				this.anonymizeOwner(personnelIds, ANONYMOUS_PERSONNEL_ID);
			}
			if(teacherIds.size() > 0){
				this.anonymizeOwner(teacherIds, ANONYMOUS_TEACHER_ID);
			}
			if (studentIds.size() > 0){
				this.anonymizeOwner(studentIds, ANONYMOUS_STUDENT_ID);
			}
			if (relativeIds.size() > 0){
				this.anonymizeOwner(relativeIds, ANONYMOUS_RELATIVE_ID);
			}
		}
	}

	private void anonymizeOwner(final JsonArray usersIds, String anonymousId){
		if (usersIds != null && usersIds.size() > 0) {
			SqlStatementsBuilder statementsBuilder = new SqlStatementsBuilder();
			statementsBuilder.prepared("UPDATE actualites.thread SET owner = ? WHERE owner IN " + Sql.listPrepared(usersIds.toArray()), new JsonArray(anonymousId).add(usersIds));
			statementsBuilder.prepared("UPDATE actualites.info SET owner = ? WHERE owner IN " + Sql.listPrepared(usersIds.toArray()), new JsonArray(anonymousId).add(usersIds));
			statementsBuilder.prepared("UPDATE actualites.comment SET owner = ? WHERE owner IN " + Sql.listPrepared(usersIds.toArray()), new JsonArray(anonymousId).add(usersIds));

			statementsBuilder.prepared("UPDATE actualites.thread_shares SET member_id = ? WHERE member_id IN " + Sql.listPrepared(usersIds.toArray()), new JsonArray(anonymousId).add(usersIds));
			statementsBuilder.prepared("UPDATE actualites.info_shares SET member_id = ? WHERE member_id IN " + Sql.listPrepared(usersIds.toArray()), usersIds);

			statementsBuilder.prepared("DELETE FROM actualites.users WHERE id IN " + Sql.listPrepared(usersIds.toArray()), usersIds);

			Sql.getInstance().transaction(statementsBuilder.build(), SqlResult.validRowsResultHandler(new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						log.info("The resources created by these users " + usersIds.toList().toString() + " are attributed to anonymous aser");
					} else {
						log.error("Error making these " + usersIds.toList().toString() + " Anonymous. Message : " + event.left().getValue());
					}
				}
			}));
		}
	}

}
