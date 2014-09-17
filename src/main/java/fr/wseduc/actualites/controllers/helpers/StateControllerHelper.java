package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import fr.wseduc.actualites.model.BaseResource;
import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.security.SecuredAction;

public class StateControllerHelper extends BaseExtractorHelper {

	private final String INFO_ID_PARAMETER = "infoid";
	private static final String NEWS_NAME = "NEWS";
	private static final String NEWS_SUBMIT_EVENT_TYPE = NEWS_NAME + "_SUBMIT";
	private static final String NEWS_UNSUBMIT_EVENT_TYPE = NEWS_NAME + "_UNSUBMIT";
	private static final String NEWS_PUBLISH_EVENT_TYPE = NEWS_NAME + "_PUBLISH";
	private static final String NEWS_UNPUBLISH_EVENT_TYPE = NEWS_NAME + "_UNPUBLISH";

	protected final InfoService infoService;
	protected final ThreadService threadService;
	protected TimelineHelper notification;

	public StateControllerHelper(final InfoService infoService, final ThreadService threadService) {
		this.infoService = infoService;
		this.threadService = threadService;
	}

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.notification = new TimelineHelper(vertx, eb, container);
	}

	public void submit(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
				@Override
				public void handle(final BaseResource model) {
					Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(final Either<String, JsonObject> event) {
							if (event.isRight()) {
								notifyTimeline(request, model.getUser(), info, model.getBody(), NEWS_SUBMIT_EVENT_TYPE);
								renderJson(request, event.right().getValue(), 200);
							} else {
								JsonObject error = new JsonObject().putString("error", event.left().getValue());
								renderJson(request, error, 400);
							}
						}
					};
					infoService.changeState(info, InfoState.PENDING, handler);
				}
			});
		}
		catch (Exception e) {
			renderErrorException(request, e);
			e.getStackTrace();
		}
	}

	public void unsubmit(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
				@Override
				public void handle(final BaseResource model) {
					Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							if (event.isRight()) {
								notifyTimeline(request, model.getUser(), info, model.getBody(), NEWS_UNSUBMIT_EVENT_TYPE);
								renderJson(request, event.right().getValue(), 200);
							} else {
								JsonObject error = new JsonObject().putString("error", event.left().getValue());
								renderJson(request, error, 400);
							}
						}
					};
					infoService.changeState(info, InfoState.DRAFT, handler);
				}
			});
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
	}

	public void publish(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
				@Override
				public void handle(final BaseResource model) {
					Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							if (event.isRight()) {
								notifyTimeline(request, model.getUser(), info, model.getBody(), NEWS_PUBLISH_EVENT_TYPE);
								renderJson(request, event.right().getValue(), 200);
							} else {
								JsonObject error = new JsonObject().putString("error", event.left().getValue());
								renderJson(request, error, 400);
							}
						}
					};
					infoService.changeState(info, InfoState.PUBLISHED, handler);
				}
			});
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
	}

	public void unpublish(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
				@Override
				public void handle(final BaseResource model) {
					Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							if (event.isRight()) {
								notifyTimeline(request, model.getUser(), info, model.getBody(), NEWS_UNPUBLISH_EVENT_TYPE);
								renderJson(request, event.right().getValue(), 200);
							} else {
								JsonObject error = new JsonObject().putString("error", event.left().getValue());
								renderJson(request, error, 400);
							}
						}
					};
					infoService.changeState(info, InfoState.PENDING, handler);
				}
			});
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
	}

	public void trash(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			infoService.changeState(info, InfoState.TRASH, notEmptyResponseHandler(request));
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
	}

	public void restore(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();

		extractThreadId(request, info);
		extractInfoId(request, info);

		try {
			infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
	}

	protected void extractInfoId(final HttpServerRequest request, final InfoResource info) {
		try {
			info.setInfoId(request.params().get(INFO_ID_PARAMETER));
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
	}

	private void notifyTimeline(final HttpServerRequest request, final UserInfos user, final InfoResource info, final JsonObject body, final String eventType){
		final String threadId = info.getThreadId();
		if (eventType == NEWS_SUBMIT_EVENT_TYPE) {
			threadService.getPublishSharedWithIds(threadId, user, new Handler<Either<String, JsonArray>>() {
				@Override
				public void handle(Either<String, JsonArray> event) {
					if (event.isRight()) {
						// get all ids
						JsonArray shared = event.right().getValue();
						extractUserIds(request, shared, user, info, body, eventType, "notify-news-submitted.html");
					}
				}
			});
		}
		else {
			if(eventType == NEWS_UNSUBMIT_EVENT_TYPE){
				threadService.getPublishSharedWithIds(threadId, user, new Handler<Either<String, JsonArray>>() {
					@Override
					public void handle(Either<String, JsonArray> event) {
						if (event.isRight()) {
							// get all ids
							JsonArray shared = event.right().getValue();
							extractUserIds(request, shared, user, info, body, eventType, "notify-news-unsubmitted.html");
						}
					}
				});
			}
			else{ // notify the owner of the new only
				if(eventType == NEWS_PUBLISH_EVENT_TYPE){
					JsonObject owner = new JsonObject();
					String userId = body.getObject("owner").getString("userId");
					owner.putString("userId", userId);
					JsonArray shared = new JsonArray();
					shared.add(owner);
					extractUserIds(request, shared, user, info, body, eventType, "notify-news-published.html");
				}
				else{
					if(eventType == NEWS_UNPUBLISH_EVENT_TYPE){
						JsonObject owner = new JsonObject();
						String userId = body.getObject("owner").getString("userId");
						owner.putString("userId", userId);
						JsonArray shared = new JsonArray();
						shared.add(owner);
						extractUserIds(request, shared, user, info, body, eventType, "notify-news-unpublished.html");
					}
				}
			}
		}
	}

	private void extractUserIds(final HttpServerRequest request, final JsonArray shared, final UserInfos user, final InfoResource info, final JsonObject body, final String eventType, final String template){
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
					if(!ids.contains(id)){
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
											if(!ids.contains(userId)){
												ids.add(userId);
											}
										}
									}
									if (remaining.decrementAndGet() < 1) {
										sendNotify(request, ids, user, info, body, eventType, template);
									}
								}
							});
						}
					}
				}
			}
			if (remaining.get() < 1) {
				sendNotify(request, ids, user, info, body, eventType, template);
			}
		}
	}

	private void sendNotify(final HttpServerRequest request, final List<String> ids, final UserInfos user, final InfoResource info, final JsonObject body, final String eventType, final String template){
		String title = body.getString("title");
		final String infoId = info.getInfoId();
		JsonObject params = new JsonObject()
			.putString("profilUri", container.config().getString("host") +
					"/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
			.putString("username", user.getUsername())
			.putString("info", title)
			.putString("actuUri", container.config().getString("host") + pathPrefix);
		if (infoId != null && !infoId.isEmpty()) {
			notification.notifyTimeline(request, user, NEWS_NAME, eventType, ids, infoId, template, params);
		}
	}
}
