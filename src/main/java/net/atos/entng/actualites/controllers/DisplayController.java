package net.atos.entng.actualites.controllers;

import java.util.Map;

import net.atos.entng.actualites.Actualites;

import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import fr.wseduc.rs.Get;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

public class DisplayController extends BaseController {

	private EventStore eventStore;
	private enum ActualitesEvent { ACCESS }

	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm,
					 Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		eventStore = EventStoreFactory.getFactory().getEventStore(Actualites.class.getSimpleName());
	}

	@Get("")
	@SecuredAction("actualites.view")
	public void view(final HttpServerRequest request) {
		renderView(request);

		// Create event "access to application Actualites" and store it in MongoDB, for module "statistics"
		eventStore.createAndStoreEvent(ActualitesEvent.ACCESS.name(), request);
	}

}
