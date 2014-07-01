package fr.wseduc.actualites.controllers.helpers;

import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.model.impl.ThreadRequestModel;
import fr.wseduc.webutils.http.BaseController;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;

public class BaseExtractorHelper extends BaseController {

	private final String THREAD_ID_PARAMETER = "thread";
	private final String INFO_ID_PARAMETER = "info";
	private final String STATE_PARAMETER = "state";
	
	protected void ensureExtractUserFromRequest(final HttpServerRequest request, final Handler<UserInfos> handler) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					handler.handle(user);
				} else {
					// log.debug("User not found in session.");
					Renders.unauthorized(request);
				}
			}
		});
	}
	
	public void ensureExtractThreadFromRequestBody(final HttpServerRequest request, final Handler<ThreadResource> handler) {
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject object) {
						try {
							handler.handle(new ThreadRequestModel(user, object));
						}
						catch (InvalidRequestException ire) {
							// log.debug("Invalid request : " + ire.getMessage());
							Renders.badRequest(request, ire.getMessage());
						}
					}
				});
			}
		});
	}
	
	protected void ensureExtractThreadFromRequestParameters(final HttpServerRequest request, final Handler<ThreadResource> handler) {
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				try {
					handler.handle(new ThreadRequestModel(
						user, 
						request.params().get(THREAD_ID_PARAMETER),
						request.params().get(STATE_PARAMETER)
					));
				}
				catch (InvalidRequestException ire) {
					// log.debug("Invalid request : " + ire.getMessage());
					Renders.badRequest(request, ire.getMessage());
				}
			}
		});
	}
	
	protected void ensureExtractInfoFromRequestBody(final HttpServerRequest request, final Handler<InfoResource> handler) {
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject object) {
						try {
							handler.handle(new InfoRequestModel(user, object));
						}
						catch (InvalidRequestException ire) {
							// log.debug("Invalid request : " + ire.getMessage());
							Renders.badRequest(request, ire.getMessage());
						}
					}
				});
			}
		});
	}
	
	protected void ensureExtractInfoFromRequestParameters(final HttpServerRequest request, final Handler<InfoResource> handler) {
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				try {
					handler.handle(new InfoRequestModel(
						user, 
						request.params().get(THREAD_ID_PARAMETER),
						request.params().get(INFO_ID_PARAMETER),
						null));
				}
				catch (InvalidRequestException ire) {
					// log.debug("Invalid request : " + ire.getMessage());
					Renders.badRequest(request, ire.getMessage());
				}
			}
		});
	}
}
