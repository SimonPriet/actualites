package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.user.UserUtils.getUserInfos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.share.ShareService;
import org.entcore.common.share.impl.MongoDbShareService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.request.RequestUtils;
import fr.wseduc.webutils.security.SecuredAction;

public class ThreadControllerHelper extends MongoDbControllerHelper {

	protected final String managedCollection;
	protected final String type;
	protected ShareService shareService;
	
	public ThreadControllerHelper(String managedCollection) {
		this(managedCollection, null);
	}
	
	public ThreadControllerHelper(String managedCollection, Map<String, List<String>> groupedActions) {
		super(managedCollection, groupedActions);
		this.managedCollection = managedCollection;
		this.type = managedCollection.toUpperCase();
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
	
		this.shareService = new MongoDbShareService(eb, mongo, managedCollection, securedActions, null);
	}
	
	@Override
	public void shareJson(final HttpServerRequest request) {
		final String id = request.params().get("id");
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					shareService.shareInfos(user.getUserId(), id,
							I18n.acceptLanguage(request), defaultResponseHandler(request));
				} else {
					unauthorized(request);
				}
			}
		});
	}
	
	@Override
	protected void shareJsonSubmit(final HttpServerRequest request, final String notifyShareTemplate) {
		final String id = request.params().get("id");
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
			@Override
			public void handle(JsonObject object) {
				final JsonArray a = object.getArray("actions");
				final String groupId = object.getString("groupId");
				final String userId = object.getString("userId");
				if (a == null || a.size() == 0) {
					badRequest(request);
					return;
				}
				final List<String> actions = new ArrayList<>();
				for (Object o: a) {
					if (o != null && o instanceof String) {
						actions.add(o.toString());
					}
				}
				getUserInfos(eb, request, new Handler<UserInfos>() {
					@Override
					public void handle(final UserInfos user) {
						if (user != null) {
							Handler<Either<String, JsonObject>> r = new Handler<Either<String, JsonObject>>() {
								@Override
								public void handle(Either<String, JsonObject> event) {
									if (event.isRight()) {
										JsonObject n = event.right().getValue()
												.getObject("notify-timeline");
										if (n != null && notifyShareTemplate != null) {
											notifyShare(request, id, user, new JsonArray().add(n),
													notifyShareTemplate);
										}
										renderJson(request, event.right().getValue());
									} else {
										JsonObject error = new JsonObject()
												.putString("error", event.left().getValue());
										renderJson(request, error, 400);
									}
								}
							};
							if (groupId != null) {
								shareService.groupShare(user.getUserId(), groupId, id, actions, r);
							} else if (userId != null) {
								shareService.userShare(user.getUserId(), userId, id, actions, r);
							} else {
								badRequest(request);
							}
						} else {
							unauthorized(request);
						}
					}
				});
			}
		});
	}

	@Override
	protected void removeShare(final HttpServerRequest request) {
		final String id = request.params().get("id");
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}

		RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
			@Override
			public void handle(JsonObject object) {
				final JsonArray a = object.getArray("actions");
				final String groupId = object.getString("groupId");
				final String userId = object.getString("userId");
				if (a == null || a.size() == 0) {
					badRequest(request);
					return;
				}
				final List<String> actions = new ArrayList<>();
				for (Object o: a) {
					if (o != null && o instanceof String) {
						actions.add(o.toString());
					}
				}
				getUserInfos(eb, request, new Handler<UserInfos>() {
					@Override
					public void handle(final UserInfos user) {
						if (user != null) {
							if (groupId != null) {
								shareService.removeGroupShare(groupId, id, actions,
										defaultResponseHandler(request));
							} else if (userId != null) {
								shareService.removeUserShare(userId, id, actions,
										defaultResponseHandler(request));
							} else {
								badRequest(request);
							}
						} else {
							unauthorized(request);
						}
					}
				});
			}
		});
	}
	
	protected void notifyShare(final HttpServerRequest request, final String resource,
			final UserInfos user, JsonArray sharedArray, final String notifyShareTemplate) {
		final List<String> recipients = new ArrayList<>();
		final AtomicInteger remaining = new AtomicInteger(sharedArray.size());
		for (Object j : sharedArray) {
			JsonObject json = (JsonObject) j;
			String userId = json.getString("userId");
			if (userId != null) {
				recipients.add(userId);
				remaining.getAndDecrement();
			} else {
				String groupId = json.getString("groupId");
				if (groupId != null) {
					UserUtils.findUsersInProfilsGroups(groupId, eb, user.getUserId(), false, new Handler<JsonArray>() {
						@Override
						public void handle(JsonArray event) {
							if (event != null) {
								for (Object o : event) {
									if (!(o instanceof JsonObject)) continue;
									JsonObject j = (JsonObject) o;
									String id = j.getString("id");
									log.debug(id);
									recipients.add(id);
								}
							}
							if (remaining.decrementAndGet() < 1) {
								sendNotify(request, resource, user, recipients, notifyShareTemplate);
							}
						}
					});
				}
			}
		}
		if (remaining.get() < 1) {
			sendNotify(request, resource, user, recipients, notifyShareTemplate);
		}
	}

	protected void sendNotify(final HttpServerRequest request, final String resource,
			final UserInfos user, final List<String> recipients, final String notifyShareTemplate) {
		final JsonObject params = new JsonObject()
				.putString("uri", container.config().getString("userbook-host") +
						"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
				.putString("username", user.getUsername())
				.putString("resourceUri", container.config().getString("host", "http://localhost:8011") +
						pathPrefix + "/document/" + resource);
		mongo.findOne(managedCollection, new JsonObject().putString("_id", resource),
				new JsonObject().putNumber("name", 1), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				if ("ok".equals(event.body().getString("status")) && event.body().getObject("result") != null) {
					params.putString("resourceName", event.body().getObject("result").getString("name", ""));
					notification.notifyTimeline(request, user, type, type + "_SHARE",
							recipients, resource, notifyShareTemplate, params);
				} else {
					log.error("Unable to send timeline notification : missing name on resource " + resource);
				}
			}
		});
	}
	
	
	public void listThreads(final HttpServerRequest request) {
		list(request);
	}
	
	public void createThread(final HttpServerRequest request) {
		create(request);
	}
	
	public void updateThread(final HttpServerRequest request) {
		update(request);
	}
	
	public void retrieveThread(final HttpServerRequest request) {
		retrieve(request);
	}
	
	public void deleteThread(final HttpServerRequest request) {
		delete(request);
	}
	
	public void shareThread(final HttpServerRequest request) {
		shareJson(request);
	}
	
	public void shareThreadSubmit(final HttpServerRequest request) {
		shareJsonSubmit(request, null);
	}
	
	public void shareThreadRemove(final HttpServerRequest request) {
		removeShare(request);
	}
}
