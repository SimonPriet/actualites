package fr.wseduc.actualites;

import fr.wseduc.actualites.controllers.ActionFilter;
import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.webutils.Server;
import fr.wseduc.webutils.request.filter.SecurityHandler;

public class Actualites extends Server {

	@Override
	public void start() {
		super.start();

		ActualitesController controller = new ActualitesController(vertx, container, rm, securedActions);
		controller.get("", "view");
		controller.get("/edit", "viewEdit");
		controller.get("/admin", "viewAdmin");
		controller.get("", "publish");
		controller.get("", "unpublish");

		SecurityHandler.addFilter(
				new ActionFilter(controller.securedUriBinding(), container.config(), vertx)
		);
	}

}
