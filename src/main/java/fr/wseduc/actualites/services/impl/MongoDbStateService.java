package fr.wseduc.actualites.services.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.services.StateService;
import fr.wseduc.webutils.Either;

public class MongoDbStateService extends AbstractService implements StateService {

	public MongoDbStateService(final String threadCollection, final String infosCollection) {
		super(threadCollection, infosCollection);
	}

	public void changeState(final InfoResource info, final InfoState targetState, final Handler<Either<String, JsonObject>> handler) {
		
	}
	
	public void retrieveState(final String id, final UserInfos user, final Handler<InfoResource> handler) {
		
	}
}
