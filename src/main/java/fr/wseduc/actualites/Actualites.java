package fr.wseduc.actualites;

import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.actualites.filters.ActualitesFilter;

import org.entcore.common.http.BaseServer;

public class Actualites extends BaseServer {

	protected final String THREADS_COLLECTION = "thread";
	protected final String ACTUALITES_COLLECTION = "actualites";
	
	@Override
	public void start() {
		setResourceProvider(new ActualitesFilter(THREADS_COLLECTION));
		super.start();
		addController(new ActualitesController(THREADS_COLLECTION, ACTUALITES_COLLECTION));
	}

}
