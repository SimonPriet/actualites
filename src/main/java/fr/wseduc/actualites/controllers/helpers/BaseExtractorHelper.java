package fr.wseduc.actualites.controllers.helpers;

import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.webutils.http.BaseController;
import fr.wseduc.webutils.http.Renders;

public class BaseExtractorHelper extends BaseController {
	
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
	
	protected void extractUserFromRequest(final HttpServerRequest request, final Handler<UserInfos> handler) {
		UserUtils.getUserInfos(eb, request, handler);
	}
}
