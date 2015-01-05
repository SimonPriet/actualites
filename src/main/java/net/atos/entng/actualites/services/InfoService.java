package net.atos.entng.actualites.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface InfoService {

	public void retrieve(String id, UserInfos user, Handler<Either<String, JsonObject>> handler);

	public void list(UserInfos user, Handler<Either<String, JsonArray>> handler);

	public void listByThreadId(String id, UserInfos user, Handler<Either<String, JsonArray>> handler);

	public void listLastPublishedInfos(UserInfos user, int resultSize, Handler<Either<String, JsonObject>> handler);

	public void listForLinker(UserInfos user, Handler<Either<String, JsonArray>> handler);

}
