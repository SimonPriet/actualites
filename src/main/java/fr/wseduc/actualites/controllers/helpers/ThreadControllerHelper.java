package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.List;
import java.util.Map;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.share.ShareService;
import org.entcore.common.share.impl.MongoDbShareService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.webutils.security.SecuredAction;

public class ThreadControllerHelper extends MongoDbControllerHelper {

	protected final String managedCollection;
	protected final String type;

	protected final ThreadService threadService;
	protected ShareService shareService;

	private static final String EVENT_TYPE = "NEWS";

	public ThreadControllerHelper(String managedCollection, final ThreadService threadService) {
		this(managedCollection, threadService, null);
	}

	public ThreadControllerHelper(String managedCollection, final ThreadService threadService, Map<String, List<String>> groupedActions) {
		super(managedCollection, groupedActions);
		this.threadService = threadService;
		this.managedCollection = managedCollection;
		this.type = managedCollection.toUpperCase();
	}

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);

		this.shareService = new MongoDbShareService(eb, mongo, managedCollection, securedActions, null);
	}

	public void listThreads(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String filter = request.params().get("filter");
				VisibilityFilter v = VisibilityFilter.ALL;
				if (filter != null) {
					try {
						v = VisibilityFilter.valueOf(filter.toUpperCase());
					} catch (IllegalArgumentException | NullPointerException e) {
						v = VisibilityFilter.ALL;
						if (log.isDebugEnabled()) {
							log.debug("Invalid filter " + filter);
						}
					}
				}
				threadService.list(v, user, arrayResponseHandler(request));
			}
		});
	}

	public void retrieveThread(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				String id = request.params().get("id");
				threadService.retrieve(id, user, notEmptyResponseHandler(request));
			}
		});
	}

	public void createThread(final HttpServerRequest request) {
		create(request);
	}

	public void updateThread(final HttpServerRequest request) {
		update(request);
	}

	public void deleteThread(final HttpServerRequest request) {
		delete(request);
	}

	public void shareThread(final HttpServerRequest request) {
		shareJson(request, false);
	}

	public void shareThreadSubmit(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					final String threadId = request.params().get("id");
					if(threadId == null || threadId.trim().isEmpty()) {
			            badRequest(request);
			            return;
			        }
					setTimelineEventType(EVENT_TYPE);
					JsonObject params = new JsonObject()
						.putString("profilUri", container.config().getString("host") +
								"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
						.putString("username", user.getUsername())
						.putString("resourceUri", container.config().getString("host") + pathPrefix +
								"#/view/thread/" + threadId);
					shareJsonSubmit(request, "notify-thread-shared.html", false, params, "title");
				} else {
					unauthorized(request);
				}
			}
		});
	}

	public void shareThreadRemove(final HttpServerRequest request) {
		removeShare(request, false);
	}
}
