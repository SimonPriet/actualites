package fr.wseduc.actualites.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import fr.wseduc.actualites.controllers.helpers.InfoControllerHelper;
import fr.wseduc.actualites.controllers.helpers.StateControllerHelper;
import fr.wseduc.actualites.controllers.helpers.ThreadControllerHelper;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.actualites.services.impl.MongoDbInfoService;
import fr.wseduc.actualites.services.impl.MongoDbThreadService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.ResourceFilter;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

public class ActualitesController extends BaseController {
	
	private final ThreadControllerHelper threadHelper;
	private final InfoControllerHelper infoHelper;
	private final StateControllerHelper stateHelper;
	private final InfoService infoService;
	private final ThreadService threadService;
	
	private final Map<String, List<String>> groupedActions;
	private final Map<String, List<InfoState>> viewStatePermissions;
	
	public ActualitesController(final String collection, final ThreadService threadService, final InfoService infoService) {
		this.infoService = infoService;
		this.threadService = threadService;
		
		this.groupedActions = new HashMap<String, List<String>>();
		this.viewStatePermissions = new HashMap<String, List<InfoState>>();
		
		this.threadHelper = new ThreadControllerHelper(collection, threadService, groupedActions);
		this.infoHelper = new InfoControllerHelper(infoService);
		this.stateHelper = new StateControllerHelper(infoService);
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		this.threadHelper.init(vertx, container, rm, securedActions);
		this.infoHelper.init(vertx, container, rm, securedActions);
		this.stateHelper.init(vertx, container, rm, securedActions);
		
		((MongoDbInfoService) this.infoService).init(vertx, container, rm, securedActions, viewStatePermissions);
		((MongoDbThreadService) this.threadService).init(vertx, container, rm, securedActions);
		
		loadGroupedActions(securedActions);
		
		// Mapping between (groupedActions alias name) and (Viewable Info states)
		List<InfoState> viewStates = new ArrayList<InfoState>();
		viewStates.add(InfoState.PUBLISHED);
		viewStatePermissions.put("thread.view", viewStates);
		
		List<InfoState> contributeStates = new ArrayList<InfoState>();
		contributeStates.add(InfoState.DRAFT);
		contributeStates.add(InfoState.PENDING);
		contributeStates.add(InfoState.PUBLISHED);
		contributeStates.add(InfoState.TRASH);
		viewStatePermissions.put("thread.contribute", contributeStates);
		
		List<InfoState> publishStates = new ArrayList<InfoState>();
		publishStates.add(InfoState.PENDING);
		publishStates.add(InfoState.PUBLISHED);
		viewStatePermissions.put("thread.publish", publishStates);
		
		List<InfoState> manageStates = new ArrayList<InfoState>();
		manageStates.add(InfoState.DRAFT);
		manageStates.add(InfoState.PENDING);
		manageStates.add(InfoState.PUBLISHED);
		manageStates.add(InfoState.TRASH);
		viewStatePermissions.put("thread.manage", manageStates);
	}

	@Get("")
	@SecuredAction("actualites.view")
	public void view(final HttpServerRequest request) {
		renderView(request);
	}
	
	@Get("/edit")
	@SecuredAction("actualites.edit")
	public void viewEdit(final HttpServerRequest request) {
		renderView(request);
	}
	
	@Get("/admin")
	@SecuredAction("actualites.admin")
	public void viewAdmin(final HttpServerRequest request) {
		renderView(request);
	}
	
	@Get("/threads")
	@ApiDoc("Get Thread by id.")
	@SecuredAction("actualites.view")
	public void listThreads(final HttpServerRequest request) {
		threadHelper.listThreads(request);
	}
	
	
	@Post("/threads")
	@ApiDoc("Create a new Thread.")
	@SecuredAction("actualites.admin")
	public void createThread(final HttpServerRequest request) {
		threadHelper.createThread(request);
	}
	
	@Get("/thread/:id")
	@ApiDoc("Get Thread by id.")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void getThread(final HttpServerRequest request) {
		threadHelper.retrieveThread(request);
	}

	@Put("/thread/:id")
	@ApiDoc("Update thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void updateThread(final HttpServerRequest request) {
		threadHelper.updateThread(request);
	}
	
	@Delete("/thread/:id")
	@ApiDoc("Delete thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void deleteThread(final HttpServerRequest request) {
		threadHelper.deleteThread(request);
	}
	
	
	@Get("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThread(final HttpServerRequest request) {
		threadHelper.shareThread(request);
	}
	
	@Put("/share/json/:id")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThreadSubmit(final HttpServerRequest request) {
		threadHelper.shareThreadSubmit(request);
	}
	
	@Put("/share/remove/:id")
	@ApiDoc("Remove Share by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThreadRemove(final HttpServerRequest request) {
		threadHelper.shareThreadRemove(request);
	}
	
	@Get("/infos")
	@ApiDoc("Get infos in thread by status and by thread id.")
	@SecuredAction("actualites.view")
	public void listInfos(final HttpServerRequest request) {
		infoHelper.listInfos(request);
	}
	
	@Get("/infos/:status")
	@ApiDoc("Get infos in thread by status and by thread id.")
	@SecuredAction("actualites.view")
	public void listInfosPublished(final HttpServerRequest request) {
		infoHelper.listInfosByStatus(request);
	}
	
	@Get("/thread/:id/:filter")
	@ApiDoc("Get infos in thread by thread id.")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void listThreadInfos(final HttpServerRequest request) {
		infoHelper.listThreadInfos(request);
	}
	
	@Get("/thread/:id/:status/:filter")
	@ApiDoc("Get infos in thread by status and by thread id.")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void listThreadInfosDraft(final HttpServerRequest request) {
		infoHelper.listThreadInfosByStatus(request);
	}
	
	@Post("/thread/:id/info")
	@ApiDoc("Add a new Info")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void createDraft(final HttpServerRequest request) {
		infoHelper.create(request);
	}
	
	@Put("/thread/:id/info/:infoid/draft")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter("stateDraft")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void updateDraft(final HttpServerRequest request) {
		infoHelper.update(request);
	}
	
	@Put("/thread/:id/info/:infoid/pending")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter("statePending")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void updatePending(final HttpServerRequest request) {
		infoHelper.update(request);
	}
	
	@Put("/thread/:id/info/:infoid/published")
	@ApiDoc("Update : update an Info in Draft state in thread by thread and by id")
	@ResourceFilter("statePublished")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void updatePublished(final HttpServerRequest request) {
		infoHelper.update(request);
	}
	
	@Delete("/thread/:id/info/:infoid")
	@ApiDoc("Delete : Real delete an Info in thread by thread and by id")
	@ResourceFilter("stateDraft")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void delete(final HttpServerRequest request) {
		infoHelper.delete(request);
	}
	
	
	@Put("/thread/:id/info/:infoid/submit")
	@ApiDoc("Submit : Change an Info to Pending state in thread by thread and by id")
	@ResourceFilter("stateDraft")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void submit(final HttpServerRequest request) {
		stateHelper.submit(request);
	}
	
	@Put("/thread/:id/info/:infoid/unsubmit")
	@ApiDoc("Cancel Submit : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter("statePending")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void unsubmit(final HttpServerRequest request) {
		stateHelper.unsubmit(request);
	}
	
	@Put("/thread/:id/info/:infoid/publish")
	@ApiDoc("Publish : Change an Info to Published state in thread by thread and by id")
	@ResourceFilter("publish")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void publish(final HttpServerRequest request) {
		stateHelper.publish(request);
	}
	
	@Put("/thread/:id/info/:infoid/unpublish")
	@ApiDoc("Unpublish : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter("statePublished")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void unpublish(final HttpServerRequest request) {
		stateHelper.unpublish(request);
	}
	
	@Put("/thread/:id/info/:infoid/thrash")
	@ApiDoc("Trash : Change an Info to Trash state in thread by thread and by id")
	@ResourceFilter("trashMine")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void trash(final HttpServerRequest request) {
		stateHelper.trash(request);
	}
	
	@Put("/thread/:id/info/:infoid/restore")
	@ApiDoc("Cancel Trash : Change an Info to Draft state in thread by thread and by id")
	@ResourceFilter("restoreMine")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void restore(final HttpServerRequest request) {
		stateHelper.restore(request);
	}
	
	@Put("/thread/:id/info/:infoid/comment")
	@ApiDoc("Cancel Trash : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.comment", type = ActionType.RESOURCE)
	public void comment(final HttpServerRequest request) {
		infoHelper.comment(request);
	}
	
	protected void loadGroupedActions(Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		for(Entry<String, fr.wseduc.webutils.security.SecuredAction> entry : securedActions.entrySet()) {
			// Only RESOURCE actions
			if (! entry.getValue().getType().equals(ActionType.RESOURCE)) {
				continue;
			}
			
			String groupName = entry.getValue().getDisplayName();
			List<String> actions;
			if (! groupedActions.containsKey(groupName)){
				actions = new ArrayList<String>();
				groupedActions.put(groupName, actions);
			}
			else {
				actions = groupedActions.get(groupName);
			}
			actions.add(entry.getValue().getName());
		}
	}
}
