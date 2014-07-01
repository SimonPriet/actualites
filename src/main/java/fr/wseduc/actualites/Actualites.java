package fr.wseduc.actualites;

import org.entcore.common.http.BaseServer;

import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.actualites.filters.ActualitesFilter;

public class Actualites extends BaseServer {

	protected final String COLLECTION = "actualites.threads";
	
	@Override
	public void start() {
		setResourceProvider(new ActualitesFilter(COLLECTION));
		super.start();
		addController(new ActualitesController(COLLECTION));
	}

}
