package net.atos.entng.actualites.services;

import java.text.ParseException;
import java.util.Map;

import net.atos.entng.actualites.model.InfoMode;
import net.atos.entng.actualites.model.InfoResource;
import net.atos.entng.actualites.model.InfoState;
import net.atos.entng.actualites.model.ThreadResource;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface InfoService {

	public void create(InfoResource info, Handler<Either<String, JsonObject>> handler) throws ParseException;

	public void retrieve(InfoResource info, Handler<Either<String, JsonObject>> handler);

	public void update(InfoResource info, Handler<Either<String, JsonObject>> handler) throws ParseException;

	public void delete(InfoResource info, Handler<Either<String, JsonObject>> handler);

	public void list(ThreadResource thread, Handler<Either<String, JsonArray>> handler);

	public void listLastPublishedInfos(UserInfos user, int resultSize, Handler<Either<String, JsonObject>> handler);

	public void listForLinker(ThreadResource thread, Handler<Either<String, JsonArray>> handler);

	public void changeState(InfoResource info, InfoState targetState, Handler<Either<String, JsonObject>> handler);

	public void addComment(InfoResource info, Handler<Either<String, JsonObject>> handler);

	public void deleteComment(InfoResource info, String commentId, Handler<Either<String, JsonObject>> handler);

	public void canDoByState(UserInfos user, String threadId, String infoId, String sharedMethod, InfoState state, Handler<Boolean> handler);

	public void canDoMineByState(UserInfos user, String threadId, String infoId, String sharedMethod, InfoState state, Handler<Boolean> handler);

	public void canDoSharedOrMineByState(UserInfos user, String threadId, String infoId, String sharedMethod, InfoState state, Handler<Boolean> handler);

	public void canDoByStatesAndModes(UserInfos user, String threadId, String infoId, String sharedMethod, Map<InfoMode, InfoState> statesAndModes, Handler<Boolean> handler);
}
