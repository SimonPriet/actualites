package fr.wseduc.actualites.controllers;


import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Controller;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.util.Map;

public class ActualitesController extends Controller {

	public ActualitesController(Vertx vertx, Container container, RouteMatcher rm,
			Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super(vertx, container, rm, securedActions);
	}

	@SecuredAction("actualites.view")
	public void view(HttpServerRequest request) {
		renderView(request);
	}
	
	@SecuredAction("actualites.edit")
	public void viewEdit(HttpServerRequest request) {
		renderView(request);
	}
	
	@SecuredAction("actualites.admin")
	public void viewAdmin(HttpServerRequest request) {
		renderView(request);
	}
	
	@SecuredAction("actualites.publish")
	public void publish(HttpServerRequest request) {
		renderJson(request, new JsonObject());
	}
	
	@SecuredAction("actualites.unpublish")
	public void unpublish(HttpServerRequest request) {
		renderJson(request, new JsonObject());
	}

}
