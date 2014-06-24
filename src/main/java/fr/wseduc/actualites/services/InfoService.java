package fr.wseduc.actualites.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.webutils.Either;

public interface InfoService extends RequestService {
	
	public void create(InfoResource info, Handler<Either<String, JsonObject>> handler);

	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void update(String id, InfoResource info, Handler<Either<String, JsonObject>> handler);

	public void delete(String id, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler);
	
	public void list(InfoState state, UserInfos user, Handler<Either<String, JsonArray>> handler);
}
