package fr.wseduc.actualites.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.webutils.Either;

public interface StateService extends RequestService {
	
	public void changeState(InfoResource info, InfoState targetState, Handler<Either<String, JsonObject>> handler);
	
	public void retrieveState(String id, UserInfos user, Handler<InfoResource> handler);
}
