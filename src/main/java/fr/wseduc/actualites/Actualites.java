package fr.wseduc.actualites;

import org.entcore.common.http.BaseServer;

import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.actualites.filters.ActualitesFilter;

public class Actualites extends BaseServer {

	protected final String THREADS_COLLECTION = "actualites.threads";
	protected final String ACTUALITES_COLLECTION = "actualites.infos";
	
	@Override
	public void start() {
		setResourceProvider(new ActualitesFilter(THREADS_COLLECTION));
		super.start();
		addController(new ActualitesController(THREADS_COLLECTION, ACTUALITES_COLLECTION));
	}

}
