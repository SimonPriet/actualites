package net.atos.entng.actualites.controllers;


import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.atos.entng.actualites.Actualites;
import net.atos.entng.actualites.filters.InfoFilter;
import net.atos.entng.actualites.services.InfoService;
import net.atos.entng.actualites.services.impl.InfoServiceSqlImpl;

import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;

public class CommentController extends ControllerHelper {

	private static final String COMMENT_ID_PARAMETER = "id";

	private static final String SCHEMA_COMMENT_CREATE = "createComment";
	private static final String SCHEMA_COMMENT_UPDATE = "updateComment";

	private static final String EVENT_TYPE = "NEWS";
	private static final String NEWS_COMMENT_EVENT_TYPE = EVENT_TYPE + "_COMMENT";
	private static final int OVERVIEW_LENGTH = 50;

	protected final InfoService infoService;

	public CommentController(){
		this.infoService = new InfoServiceSqlImpl();
	}

	@Put("/info/:"+Actualites.INFO_RESOURCE_ID+"/comment")
	@ApiDoc("Comment : Add a comment to an Info by info id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "info.comment", type = ActionType.RESOURCE)
	public void comment(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_COMMENT_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						final String commentText = resource.getString("comment");
						final String title = resource.getString("title");
						resource.removeField("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									JsonObject comment = event.right().getValue();
									String commentId = comment.getNumber("id").toString();
									notifyTimeline(request, user, infoId, commentId, title, commentText, NEWS_COMMENT_EVENT_TYPE);
									renderJson(request, event.right().getValue(), 200);
								} else {
									JsonObject error = new JsonObject().putString("error", event.left().getValue());
									renderJson(request, error, 400);
								}
							}
						};
						crudService.create(resource, user, handler);
					}
				});
			}
		});
	}

	@Put("/info/:"+Actualites.INFO_RESOURCE_ID+"/comment/:"+COMMENT_ID_PARAMETER)
	@ApiDoc("Comment : modify a comment of an Info by info and comment id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "info.comment", type = ActionType.RESOURCE)
	public void updateComment(final HttpServerRequest request) {
		final String commentId = request.params().get(COMMENT_ID_PARAMETER);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_COMMENT_UPDATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						crudService.update(commentId, resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Delete("/info/:"+Actualites.INFO_RESOURCE_ID+"/comment/:"+COMMENT_ID_PARAMETER)
	@ApiDoc("Comment : delete a comment by comment id ")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "info.comment", type = ActionType.RESOURCE)
	public void deleteComment(final HttpServerRequest request) {
		final String commentId = request.params().get(COMMENT_ID_PARAMETER);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				crudService.delete(commentId, user,notEmptyResponseHandler(request));
			}
		});
	}

	private void notifyTimeline(final HttpServerRequest request, final UserInfos user, final String infoId, final String commentId, final String title, final String commentText, final String eventType){
		if (eventType.equals(NEWS_COMMENT_EVENT_TYPE)) {
			infoService.getSharedWithIds(infoId, new Handler<Either<String, JsonArray>>() {
				@Override
				public void handle(Either<String, JsonArray> event) {
					if (event.isRight()) {
						// get all ids
						JsonArray shared = event.right().getValue();
						extractUserIds(request, shared, user, infoId, commentId, title, commentText, "news.news-comment");
					}
				}
			});
		}
	}

	private void extractUserIds(final HttpServerRequest request, final JsonArray shared, final UserInfos user, final String infoId, final String commentId, final String title, final String commentText, final String notificationName){
		final List<String> ids = new ArrayList<String>();
		if (shared.size() > 0) {
			JsonObject jo = null;
			String groupId = null;
			String id = null;
			final AtomicInteger remaining = new AtomicInteger(shared.size());
			// Extract shared with
			for(int i=0; i<shared.size(); i++){
				jo = (JsonObject) shared.get(i);
				if(jo.containsField("userId")){
					id = jo.getString("userId");
					if(!ids.contains(id) && !(user.getUserId().equals(id))){
						ids.add(id);
					}
					remaining.getAndDecrement();
				}
				else{
					if(jo.containsField("groupId")){
						groupId = jo.getString("groupId");
						if (groupId != null) {
							UserUtils.findUsersInProfilsGroups(groupId, eb, user.getUserId(), false, new Handler<JsonArray>() {
								@Override
								public void handle(JsonArray event) {
									if (event != null) {
										String userId = null;
										for (Object o : event) {
											if (!(o instanceof JsonObject)) continue;
											userId = ((JsonObject) o).getString("id");
											if(!ids.contains(userId) && !(user.getUserId().equals(userId))){
												ids.add(userId);
											}
										}
									}
									if (remaining.decrementAndGet() < 1) {
										sendNotify(request, ids, user, infoId, commentId, title, commentText, notificationName);
									}
								}
							});
						}
					}
				}
			}
			if (remaining.get() < 1) {
				sendNotify(request, ids, user, infoId, commentId, title, commentText, notificationName);
			}
		}
	}

	private void sendNotify(final HttpServerRequest request, final List<String> ids, final UserInfos user, final String infoId, final String commentId, final String title, String commentText, final String notificationName){
		if (infoId != null && !infoId.isEmpty() && commentId != null && !commentId.isEmpty() && user != null && !commentText.isEmpty()) {
			String overview = commentText.replaceAll("<br>", "");
			overview = "<p>".concat(overview);
			if(overview.length() > OVERVIEW_LENGTH){
				overview = overview.substring(0, OVERVIEW_LENGTH);
			}
			overview = overview.concat("</p>");
			JsonObject params = new JsonObject()
				.putString("profilUri", "/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
				.putString("username", user.getUsername())
				.putString("info", title)
				.putString("actuUri", pathPrefix + "#/view/info/" + infoId + "/comment/" + commentId)
				.putString("overview", overview);
			notification.notifyTimeline(request, notificationName, user, ids, infoId, params);
		}
	}

}
