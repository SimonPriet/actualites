package net.atos.entng.actualites.controllers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;
import static org.entcore.common.user.UserUtils.getUserInfos;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import net.atos.entng.actualites.Actualites;
import net.atos.entng.actualites.filters.InfoFilter;
import net.atos.entng.actualites.filters.ThreadFilter;
import net.atos.entng.actualites.services.InfoService;
import net.atos.entng.actualites.services.ThreadService;
import net.atos.entng.actualites.services.impl.InfoServiceSqlImpl;
import net.atos.entng.actualites.services.impl.ThreadServiceSqlImpl;

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
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.request.RequestUtils;

public class InfoController extends ControllerHelper {

	private static final String INFO_ID_PARAMETER = "id";
	private static final String RESULT_SIZE_PARAMETER = "resultSize";

	private static final String SCHEMA_INFO_CREATE = "createInfo";
	private static final String SCHEMA_INFO_UPDATE = "updateInfo";

	private static final String RESOURCE_NAME = "info";
	private static final String EVENT_TYPE = "NEWS";
	private static final String NEWS_SUBMIT_EVENT_TYPE = EVENT_TYPE + "_SUBMIT";
	private static final String NEWS_UNSUBMIT_EVENT_TYPE = EVENT_TYPE + "_UNSUBMIT";
	private static final String NEWS_PUBLISH_EVENT_TYPE = EVENT_TYPE + "_PUBLISH";
	private static final String NEWS_UNPUBLISH_EVENT_TYPE = EVENT_TYPE + "_UNPUBLISH";


	// TRASH: 1; DRAFT: 1; PENDING: 2; PUBLISHED: 3
	private static final List<Integer> status_list = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3));

	protected final InfoService infoService;
	protected final ThreadService threadService;

	public InfoController(){
		this.infoService = new InfoServiceSqlImpl();
		this.threadService = new ThreadServiceSqlImpl();
	}

	@Get("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID)
	@ApiDoc("Retrieve : retrieve an Info in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "info.read", type = ActionType.RESOURCE)
	public void getInfo(final HttpServerRequest request) {
		// TODO IMPROVE @SecuredAction : Security on Info as a resource
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				infoService.retrieve(infoId, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Get("/infos")
	@ApiDoc("Get infos.")
	@SecuredAction("actualites.infos.list")
	public void listInfos(final HttpServerRequest request) {
		// TODO IMPROVE : Security on Infos visibles by statuses / dates is not enforced
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(UserInfos user) {
				infoService.list(user, arrayResponseHandler(request));
			}
		});
	}

	@Get("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/infos")
	@ApiDoc("Get infos in thread by thread id.")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.read", type = ActionType.RESOURCE)
	public void listInfosByThreadId(final HttpServerRequest request) {
		// TODO IMPROVE : Security on Infos visibles by statuses / dates is not enforced
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(UserInfos user) {
				infoService.listByThreadId(threadId, user, arrayResponseHandler(request));
			}
		});
	}

	@Get("/linker/infos")
	@ApiDoc("List infos without their content. Used by linker")
	@SecuredAction("actualites.infos.list")
	public void listInfosForLinker(final HttpServerRequest request) {
		// TODO IMPROVE : Security on Infos visibles by statuses / dates is not enforced
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(UserInfos user) {
				infoService.listForLinker(user, arrayResponseHandler(request));
			}
		});
	}

	@Get("/infos/last/:"+RESULT_SIZE_PARAMETER)
	@ApiDoc("Get infos in thread by status and by thread id.")
	@SecuredAction("actualites.infos.list")
	public void listLastPublishedInfos(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(UserInfos user) {
				String resultSize = request.params().get(RESULT_SIZE_PARAMETER);
				int size;
				if (resultSize == null || resultSize.trim().isEmpty()) {
					badRequest(request);
					return;
				}
				else {
					try {
						size = Integer.parseInt(resultSize);
					} catch (NumberFormatException e) {
						badRequest(request, "actualites.widget.bad.request.size.must.be.an.integer");
						return;
					}
					if(size <=0 || size > 20) {
						badRequest(request, "actualites.widget.bad.request.invalid.size");
						return;
					}
				}
				infoService.listLastPublishedInfos(user, size, arrayResponseHandler(request));
			}
		});
	}

	@Post("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info")
	@ApiDoc("Add a new Info with draft status")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void createDraft(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(1));
						crudService.create(resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Post("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/pending")
	@ApiDoc("Add a new Info with pending status")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void createPending(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(2));
						final String threadId = resource.getNumber("thread_id").toString();
						final String title = resource.getString("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									JsonObject info = event.right().getValue();
									String infoId = info.getNumber("id").toString();
									notifyTimeline(request, user, threadId, infoId, title, NEWS_SUBMIT_EVENT_TYPE);
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

	@Post("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/published")
	@ApiDoc("Add a new Info published status")
	@ResourceFilter(ThreadFilter.class)
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void createPublished(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_CREATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(3));
						crudService.create(resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/draft")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void updateDraft(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_UPDATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(1));
						crudService.update(infoId, resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/pending")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void updatePending(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_UPDATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(2));
						crudService.update(infoId, resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/published")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void updatePublished(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_INFO_UPDATE, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject resource) {
						resource.putNumber("status", status_list.get(3));
						crudService.update(infoId, resource, user, notEmptyResponseHandler(request));
					}
				});
			}
		});
	}

	@Override
	@Delete("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID)
	@ApiDoc("Delete : Real delete an Info in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.manager", type = ActionType.RESOURCE)
	public void delete(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				crudService.delete(infoId, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/submit")
	@ApiDoc("Submit : Change an Info to Pending state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void submit(final HttpServerRequest request) {
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject body) {
						final String title = body.getString("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									notifyTimeline(request, user, threadId, infoId, title, NEWS_SUBMIT_EVENT_TYPE);
									renderJson(request, event.right().getValue(), 200);
								} else {
									JsonObject error = new JsonObject().putString("error", event.left().getValue());
									renderJson(request, error, 400);
								}
							}
						};
						JsonObject resource = new JsonObject();
						resource.putNumber("status", status_list.get(2));
						crudService.update(infoId, resource, user, handler);
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/unsubmit")
	@ApiDoc("Cancel Submit : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void unsubmit(final HttpServerRequest request) {
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject body) {
						final String title = body.getString("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									notifyTimeline(request, user, threadId, infoId, title, NEWS_UNSUBMIT_EVENT_TYPE);
									renderJson(request, event.right().getValue(), 200);
								} else {
									JsonObject error = new JsonObject().putString("error", event.left().getValue());
									renderJson(request, error, 400);
								}
							}
						};
						JsonObject resource = new JsonObject();
						resource.putNumber("status", status_list.get(1));
						crudService.update(infoId, resource, user, handler);
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/publish")
	@ApiDoc("Publish : Change an Info to Published state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void publish(final HttpServerRequest request) {
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject body) {
						final String title = body.getString("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									notifyTimeline(request, user, threadId, infoId, title, NEWS_PUBLISH_EVENT_TYPE);
									renderJson(request, event.right().getValue(), 200);
								} else {
									JsonObject error = new JsonObject().putString("error", event.left().getValue());
									renderJson(request, error, 400);
								}
							}
						};
						JsonObject resource = new JsonObject();
						resource.putNumber("status", status_list.get(3));
						crudService.update(infoId, resource, user, handler);
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/unpublish")
	@ApiDoc("Unpublish : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void unpublish(final HttpServerRequest request) {
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject body) {
						final String title = body.getString("title");
						Handler<Either<String, JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
							@Override
							public void handle(Either<String, JsonObject> event) {
								if (event.isRight()) {
									notifyTimeline(request, user, threadId, infoId, title, NEWS_UNPUBLISH_EVENT_TYPE);
									renderJson(request, event.right().getValue(), 200);
								} else {
									JsonObject error = new JsonObject().putString("error", event.left().getValue());
									renderJson(request, error, 400);
								}
							}
						};
						JsonObject resource = new JsonObject();
						resource.putNumber("status", status_list.get(2));
						crudService.update(infoId, resource, user, handler);
					}
				});
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"/thrash")
	@ApiDoc("Trash : Change an Info to Trash state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void trash(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				JsonObject resource = new JsonObject();
				resource.putNumber("status", status_list.get(0));
				crudService.update(infoId, resource, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/:"+Actualites.INFO_RESOURCE_ID+"restore")
	@ApiDoc("Cancel Trash : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void restore(final HttpServerRequest request) {
		final String infoId = request.params().get(Actualites.INFO_RESOURCE_ID);
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				JsonObject resource = new JsonObject();
				resource.putNumber("status", status_list.get(1));
				crudService.update(infoId, resource, user, notEmptyResponseHandler(request));
			}
		});
	}

	@Get("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/share/json/:"+INFO_ID_PARAMETER)
	@ApiDoc("Get shared info by id.")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void shareInfo(final HttpServerRequest request) {
		final String id = request.params().get(INFO_ID_PARAMETER);
		if (id == null || id.trim().isEmpty()) {
			badRequest(request);
			return;
		}
		getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					shareService.shareInfos(user.getUserId(), id, I18n.acceptLanguage(request), new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							final Handler<Either<String, JsonObject>> handler = defaultResponseHandler(request);
							if(event.isRight()){
								JsonObject result = event.right().getValue();
								if(result.containsField("actions")){
									JsonArray actions = result.getArray("actions");
									JsonArray newActions = new JsonArray();
									for(Object action : actions){
										if(((JsonObject) action).containsField("displayName")){
											String displayName = ((JsonObject) action).getString("displayName");
											if(displayName.contains(".")){
												String resource = displayName.split("\\.")[0];
												if(resource.equals(RESOURCE_NAME)){
													newActions.add(action);
												}
											}
										}
									}
									result.putArray("actions", newActions);
								}
								handler.handle(new Either.Right<String, JsonObject>(result));
							} else {
								handler.handle(new Either.Left<String, JsonObject>("Error finding shared resource."));
							}
						}
					});

				} else {
					unauthorized(request);
				}
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/share/json/:"+INFO_ID_PARAMETER)
	@ApiDoc("Share info by id.")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void shareInfoSubmit(final HttpServerRequest request) {
		final String threadId = request.params().get(Actualites.THREAD_RESOURCE_ID);
		final String infoId = request.params().get(INFO_ID_PARAMETER);
		if(threadId == null || threadId.trim().isEmpty()
			|| infoId == null || infoId.trim().isEmpty()) {
            badRequest(request);
            return;
        }
		request.pause();
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					infoService.retrieve(infoId, user, new Handler<Either<String, JsonObject>>() {
						@Override
						public void handle(Either<String, JsonObject> event) {
							request.resume();
							if(event.right() != null){
								JsonObject info = event.right().getValue();
								if(info != null && info.containsField("status")){
									if(info.getInteger("status") > 2){
										// notify only when info is published
										setTimelineEventType(EVENT_TYPE);
										JsonObject params = new JsonObject()
											.putString("profilUri", container.config().getString("host") + "/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
											.putString("username", user.getUsername())
											.putString("resourceUri", container.config().getString("host") + pathPrefix + "#/view/thread/" + threadId + "/info/" + infoId);
										DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
										String date = info.getString("publication_date");
										if(date != null && !date.trim().isEmpty()){
											try {
												Date publication_date = dfm.parse(date);
												Date timeNow=new Date(System.currentTimeMillis());
												if(publication_date.after(timeNow)){
													params.putNumber("timeline-publish-date", publication_date.getTime());
												}
											} catch (ParseException e) {
												e.printStackTrace();
											}
										}
										shareJsonSubmit(request, "notify-info-shared.html", false, params, "title");
									} else {
										shareJsonSubmit(request, null, false, null, null);
									}
								}
							}
						}
					});
				} else {
					unauthorized(request);
				}
			}
		});
	}

	@Put("/thread/:"+Actualites.THREAD_RESOURCE_ID+"/info/share/remove/:"+INFO_ID_PARAMETER)
	@ApiDoc("Remove Share by id.")
	@ResourceFilter(InfoFilter.class)
	@SecuredAction(value = "thread.contrib", type = ActionType.RESOURCE)
	public void shareInfoRemove(final HttpServerRequest request) {
		removeShare(request, false);
	}

	private void notifyTimeline(final HttpServerRequest request, final UserInfos user, final String threadId, final String infoId, final String title, final String eventType){
		if (eventType.equals(NEWS_SUBMIT_EVENT_TYPE)) {
			threadService.getPublishSharedWithIds(threadId, user, new Handler<Either<String, JsonArray>>() {
				@Override
				public void handle(Either<String, JsonArray> event) {
					if (event.isRight()) {
						// get all ids
						JsonArray shared = event.right().getValue();
						extractUserIds(request, shared, user, threadId, infoId, title, eventType, "notify-news-submitted.html");
					}
				}
			});
		}
		else {
			if(eventType.equals(NEWS_UNSUBMIT_EVENT_TYPE)){
				threadService.getPublishSharedWithIds(threadId, user, new Handler<Either<String, JsonArray>>() {
					@Override
					public void handle(Either<String, JsonArray> event) {
						if (event.isRight()) {
							// get all ids
							JsonArray shared = event.right().getValue();
							extractUserIds(request, shared, user, threadId, infoId, title, eventType, "notify-news-unsubmitted.html");
						}
					}
				});
			}
			else{
				if(eventType.equals(NEWS_PUBLISH_EVENT_TYPE)){
					infoService.getSharedWithIds(infoId, user, new Handler<Either<String, JsonArray>>() {
						@Override
						public void handle(Either<String, JsonArray> event) {
							if (event.isRight()) {
								// get all ids
								JsonArray shared = event.right().getValue();
								extractUserIds(request, shared, user, threadId, infoId, title, eventType, "notify-news-published.html");
							}
						}
					});
				}
				else{
					if(eventType.equals(NEWS_UNPUBLISH_EVENT_TYPE)){
						infoService.getSharedWithIds(infoId, user, new Handler<Either<String, JsonArray>>() {
							@Override
							public void handle(Either<String, JsonArray> event) {
								if (event.isRight()) {
									// get all ids
									JsonArray shared = event.right().getValue();
									extractUserIds(request, shared, user, threadId, infoId, title, eventType, "notify-news-unpublished.html");
								}
							}
						});
					}
				}
			}
		}
	}

	private void extractUserIds(final HttpServerRequest request, final JsonArray shared, final UserInfos user, final String threadId, final String infoId, final String title, final String eventType, final String template){
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
										sendNotify(request, ids, user, threadId, infoId, title, eventType, template);
									}
								}
							});
						}
					}
				}
			}
			if (remaining.get() < 1) {
				sendNotify(request, ids, user, threadId, infoId, title, eventType, template);
			}
		}
	}

	private void sendNotify(final HttpServerRequest request, final List<String> ids, final UserInfos user, final String threadId, final String infoId, final String title, final String eventType, final String template){
		if (infoId != null && !infoId.isEmpty() && threadId != null && !threadId.isEmpty() && user != null) {
			JsonObject params = new JsonObject()
				.putString("profilUri", container.config().getString("host") + "/userbook/annuaire#" + user.getUserId() + "#" + user.getType())
				.putString("username", user.getUsername())
				.putString("info", title)
				.putString("actuUri", container.config().getString("host") + pathPrefix + "#/view/thread/" + threadId + "/info/" + infoId);
			notification.notifyTimeline(request, user, EVENT_TYPE, eventType, ids, infoId, template, params);
		}
	}

}
