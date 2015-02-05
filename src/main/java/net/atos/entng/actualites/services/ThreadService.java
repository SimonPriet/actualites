package net.atos.entng.actualites.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface ThreadService {

	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler);

	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void getPublishSharedWithIds(String categoryId, UserInfos user, Handler<Either<String, JsonArray>> handler);

}
