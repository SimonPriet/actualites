package fr.wseduc.actualites;

import org.entcore.common.http.BaseServer;

import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.actualites.filters.ActualitesFilter;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.actualites.services.impl.MongoDbInfoService;
import fr.wseduc.actualites.services.impl.MongoDbThreadService;

public class Actualites extends BaseServer {

	protected final String COLLECTION = "actualites.threads";
	
	@Override
	public void start() {
		
		final InfoService infoService = new MongoDbInfoService(COLLECTION);
		final ThreadService threadService = new MongoDbThreadService(COLLECTION);
		
		setResourceProvider(new ActualitesFilter(COLLECTION, infoService));
		super.start();
		addController(new ActualitesController(COLLECTION, threadService, infoService));
	}

}
