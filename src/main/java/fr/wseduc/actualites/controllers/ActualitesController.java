package fr.wseduc.actualites.controllers;


import fr.wseduc.actualites.services.ActualitesService;
import fr.wseduc.actualites.services.StateService;
import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.ResourceFilter;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Controller;
import fr.wseduc.webutils.http.BaseController;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.util.Map;

public class ActualitesController extends MongoDbControllerHelper {

	protected final ThreadService threadService;
	protected final ActualitesService actualitesService;
	protected final StateService stateService;
	
	
	public ActualitesController(final String threadCollection, final String actualitesCollection) {
		super(threadCollection);
		
		this.threadService = new ThreadService(this.mongo, threadCollection, actualitesCollection);
		this.actualitesService = new ActualitesService(this.mongo, threadCollection, actualitesCollection);
		this.stateService = new StateService(this.mongo, threadCollection, actualitesCollection);
	}

	@Get("")
	@SecuredAction("actualites.view")
	public void view(HttpServerRequest request) {
		renderView(request);
	}
	
	@Get("/edit")
	@SecuredAction("actualites.edit")
	public void viewEdit(HttpServerRequest request) {
		renderView(request);
	}
	
	@Get("/admin")
	@SecuredAction("actualites.admin")
	public void viewAdmin(HttpServerRequest request) {
		renderView(request);
	}
	
	
	@Get("/threads")
	@ApiDoc("Get Thread by id.")
	@SecuredAction("threads.list")
	public void listThreads(HttpServerRequest request) {
		super.list(request);
	}
	
	
	@Post("/threads")
	@ApiDoc("Create a new Thread.")
	@SecuredAction("actualites.admin")
	public void createThread(HttpServerRequest request) {
		create(request);
	}
	
	@Get("/thread/:id")
	@ApiDoc("Get Thread by id.")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void getThread(HttpServerRequest request) {
		retrieve(request);
	}

	@Put("/thread/:id")
	@ApiDoc("Update thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void updateThread(HttpServerRequest request) {
		super.update(request);
	}
	
	@Delete("/thread/:id")
	@ApiDoc("Delete thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void deleteThread(HttpServerRequest request) {
		super.delete(request);
	}
	
	@Get("/actualites/thread/:id")
	@ApiDoc("Get actualites in thread by thread id.")
	@ResourceFilter("allStatusFilter")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void getThreadActualites(HttpServerRequest request) {
		// TODO: IMPLEMENT getThreadActualites
	}
	
	
	@Get("/thread/:id/share")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThread(HttpServerRequest request) {
		// TODO: IMPLEMENT shareThread
	}
	
	@Put("/thread/:id/share")
	@ApiDoc("Share thread by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThreadSubmit(HttpServerRequest request) {
		// TODO: IMPLEMENT shareThreadSubmit
	}
	
	@Put("/thread/share/remove/:id")
	@ApiDoc("Remove Share by id.")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void shareThreadRemove(HttpServerRequest request) {
		// TODO: IMPLEMENT shareRemoveSubmit
	}
	
	
	@Get("/actualites/state/:status/:id")
	@ApiDoc("Get actualites in thread by status and by thread id.")
	@ResourceFilter("statusFilter")
	@SecuredAction(value = "thread.view", type = ActionType.RESOURCE)
	public void getThreadActualitesByStatus(HttpServerRequest request) {
		// TODO: IMPLEMENT getThreadActualitesByStatus
	}
	
	@Post("/actualite/:thread/drafts")
	@ApiDoc("Add a new Actualite")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void createDraft(HttpServerRequest request) {
		// TODO: IMPLEMENT createDraft
	}
	
	@Put("/actualite/:thread/submit/:id")
	@ApiDoc("Submit : Change a Actualite to Pending state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void submit(HttpServerRequest request) {
		// TODO: IMPLEMENT submit
	}
	
	@Put("/actualite/:thread/unsubmit/:id")
	@ApiDoc("Cancel Submit : Change a Actualite to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void unsubmit(HttpServerRequest request) {
		// TODO: IMPLEMENT unsubmit
	}
	
	@Put("/actualite/:thread/publish/:id")
	@ApiDoc("Publish : Change a Actualite to Published state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void publish(HttpServerRequest request) {
		// TODO: IMPLEMENT publish
	}
	
	@Put("/actualite/:thread/unpublish/:id")
	@ApiDoc("Unpublish : Change a Actualite to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void unpublish(HttpServerRequest request) {
		// TODO: IMPLEMENT unpublish
	}
	
	@Put("/actualite/:thread/thrash/:id")
	@ApiDoc("Trash : Change a Actualite to Trash state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void trash(HttpServerRequest request) {
		// TODO: IMPLEMENT trash
	}
	
	@Put("/actualite/:thread/restore/:id")
	@ApiDoc("Cancel Trash : Change a Actualite to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void restore(HttpServerRequest request) {
		// TODO: IMPLEMENT restore
	}
	
	@Delete("/actualite/:thread/delete/:id")
	@ApiDoc("Delete : Real delete an Actualite in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void delete(HttpServerRequest request) {
		// TODO: IMPLEMENT delete
	}
}
