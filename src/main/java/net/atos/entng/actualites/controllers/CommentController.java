package net.atos.entng.actualites.controllers;


import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import net.atos.entng.actualites.filters.InfoFilter;

import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;

public class CommentController extends ControllerHelper {

	private static final String INFO_ID_PARAMETER = "id";
	private static final String COMMENT_ID_PARAMETER = "commentid";

	private static final String SCHEMA_COMMENT_CREATE = "createComment";
	private static final String SCHEMA_COMMENT_UPDATE = "updateComment";

	@Put("/info/:"+INFO_ID_PARAMETER+"/comment")
	@ApiDoc("Comment : Add a comment to an Info by info id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "info.comment", type = ActionType.RESOURCE)
	public void comment(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_COMMENT_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						crudService.create(resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Put("/info/:"+INFO_ID_PARAMETER+"/comment/:"+COMMENT_ID_PARAMETER)
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

	@Delete("/info/:"+INFO_ID_PARAMETER+"/comment/:"+COMMENT_ID_PARAMETER)
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

}
