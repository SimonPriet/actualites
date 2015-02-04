package net.atos.entng.actualites.services.impl;

import java.util.ArrayList;
import java.util.List;

import net.atos.entng.actualites.services.InfoService;
import net.atos.entng.actualites.services.ThreadService;

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
	protected final ThreadService threadService;
	protected final InfoService infoService;

	public ActualitesRepositoryEvents(boolean shareOldGroupsToUsers) {
		this.shareOldGroupsToUsers = shareOldGroupsToUsers;
		this.threadService = new ThreadServiceSqlImpl();
		this.infoService = new InfoServiceSqlImpl();
	}

	@Override
	public void exportResources(String exportId, String userId, JsonArray groups, String exportPath, String locale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteGroups(JsonArray groups) {
		if (shareOldGroupsToUsers && groups != null && groups.size() > 0){
			final List<String> gIds = new ArrayList<>();
			for (Object o : groups) {
				if (!(o instanceof JsonObject)) continue;
				final JsonObject j = (JsonObject) o;
				gIds.add(j.getString("group"));
			}
			// Remove the shared rights on Threads
			threadService.removeAllGroupsShares(gIds, new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						log.info("The (Threads) rights shared with these groups " + gIds.toString() + " are removed");
					} else {
						log.error("Error removing (Threads) rights of these groups " + gIds.toString() + ". Message : " + event.left().getValue());
					}
				}
			});
			// Remove the shared rights on News
			infoService.removeAllInfosShares(gIds, new Handler<Either<String, JsonObject>>() {
				@Override
				public void handle(Either<String, JsonObject> event) {
					if (event.isRight()) {
						log.info("The (News) rights shared with these groups " + gIds.toString() + " are removed");
					} else {
						log.error("Error removing (News) rights of these groups " + gIds.toString() + ". Message : " + event.left().getValue());
					}
				}
			});
		}
	}

	@Override
	public void deleteUsers(JsonArray users) {
		// TODO Auto-generated method stub

	}

}
