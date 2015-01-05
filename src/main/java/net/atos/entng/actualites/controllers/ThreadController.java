package net.atos.entng.actualites.controllers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;
import net.atos.entng.actualites.filters.ThreadFilter;
import net.atos.entng.actualites.services.ThreadService;
import net.atos.entng.actualites.services.impl.ThreadServiceSqlImpl;

import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;

public class ThreadController extends ControllerHelper {

	private static final String THREAD_ID_PARAMETER = "id";

	private static final String SCHEMA_THREAD_CREATE = "createThread";
	private static final String SCHEMA_THREAD_UPDATE = "updateThread";

	private static final String EVENT_TYPE = "NEWS";
	protected final ThreadService threadService;

	public ThreadController(){
		this.threadService = new ThreadServiceSqlImpl();
	}

	@Get("/threads")
	@ApiDoc("Get Thread by id.")
	@SecuredAction("thread.list")
	public void listThreads(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				threadService.list(user, arrayResponseHandler(request));
			}
		});
	}

	@Post("/thread")
	@ApiDoc("Create a new Thread.")
	@SecuredAction("thread.create")
	public void createThread(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_THREAD_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						crudService.create(resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Get("/thread/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Get Thread by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.read", type = ActionType.RESOURCE)
	public void getThread(final HttpServerRequest request) {
		final String threadId = request.params().get(THREAD_ID_PARAMETER);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				threadService.retrieve(threadId, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Put("/thread/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Update thread by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void updateThread(final HttpServerRequest request) {
		final String threadId = request.params().get(THREAD_ID_PARAMETER);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_THREAD_UPDATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						crudService.update(threadId, resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Delete("/thread/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Delete thread by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void deleteThread(final HttpServerRequest request) {
		final String threadId = request.params().get(THREAD_ID_PARAMETER);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				crudService.delete(threadId, user, notEmptyResponseHandler(request));
			}
		});
	}


	@Get("/share/json/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Share thread by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void shareThread(final HttpServerRequest request) {
		shareJson(request, false);
	}

	@Put("/share/json/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Share thread by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void shareThreadSubmit(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					final String threadId = request.params().get(THREAD_ID_PARAMETER);
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

	@Put("/share/remove/:" + THREAD_ID_PARAMETER)
	@ApiDoc("Remove Share by id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void shareThreadRemove(final HttpServerRequest request) {
		removeShare(request, false);
	}
}
