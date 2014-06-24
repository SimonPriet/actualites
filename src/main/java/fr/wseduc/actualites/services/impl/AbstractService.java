package fr.wseduc.actualites.services.impl;

import java.util.Map;

import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.model.impl.ThreadRequestModel;
import fr.wseduc.actualites.services.RequestService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import fr.wseduc.webutils.security.SecuredAction;

public abstract class AbstractService implements RequestService {

	protected final String threadCollection;
	protected final String actualitesCollection;
	
	protected EventBus eb;
	protected MongoDb mongo;
	protected TimelineHelper notification;
	
	public AbstractService(final String threadCollection, final String actualitesCollection) {
		this.threadCollection = threadCollection;
		this.actualitesCollection = actualitesCollection;
	}
	
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		this.eb = vertx.eventBus();
		this.mongo = MongoDb.getInstance();
		this.notification = new TimelineHelper(vertx, eb, container);
	}
	
	@Override
	public void ensureExtractUser(final HttpServerRequest request, final Handler<UserInfos> handler) {
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
	
	@Override
	public void ensureExtractThreadModel(final HttpServerRequest request, final Handler<ThreadResource> handler) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
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
				} else {
					// log.debug("User not found in session.");
					Renders.unauthorized(request);
				}
			}
		});
	}
	
	@Override
	public void ensureExtractInfoModel(final HttpServerRequest request, final Handler<InfoResource> handler) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
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
				} else {
					// log.debug("User not found in session.");
					Renders.unauthorized(request);
				}
			}
		});
	}
}
