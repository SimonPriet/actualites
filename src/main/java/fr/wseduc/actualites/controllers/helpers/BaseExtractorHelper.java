package fr.wseduc.actualites.controllers.helpers;

import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.BaseResource;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.webutils.http.BaseController;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;

public class BaseExtractorHelper extends BaseController {
	
	private static final String THREAD_ID_PARAMETER = "id";
	
	protected void extractUserFromRequest(final BaseResource model, final HttpServerRequest request, final Handler<BaseResource> handler) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				try {
					model.setUser(user);
					handler.handle(model);
				}
				catch (InvalidRequestException ire){
					log.error("Failed to extract User" + ire.getMessage(), ire);
					Renders.unauthorized(request);
				}
			}
		});
	}
	
	protected void extractUserAndBodyFromRequest(final BaseResource model, final HttpServerRequest request, final Handler<BaseResource> handler) {
		extractUserFromRequest(model, request, new Handler<BaseResource>(){
			@Override
			public void handle(final BaseResource model) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject object) {
						try {
							model.setBody(object);
							handler.handle(model);
						}
						catch (InvalidRequestException ire) {
							log.error("Invalid request : " + ire.getMessage());
							Renders.badRequest(request, ire.getMessage());
						}
					}
				});
			}
		});
	}
	
	protected void extractThreadId(final HttpServerRequest request, final BaseResource model) {
		try {
			model.setThreadId(request.params().get(THREAD_ID_PARAMETER));
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
	}
}
