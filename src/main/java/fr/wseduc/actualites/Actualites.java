package fr.wseduc.actualites;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;

import fr.wseduc.actualites.controllers.ActualitesController;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.ThreadService;
import fr.wseduc.actualites.services.impl.MongoDbInfoService;
import fr.wseduc.actualites.services.impl.MongoDbThreadService;

public class Actualites extends BaseServer {

	public static final String COLLECTION = "actualites.threads";
	
	@Override
	public void start() {
		
		final MongoDbConf conf = MongoDbConf.getInstance();
		conf.setCollection(COLLECTION);
		conf.setResourceIdLabel("id");
		
		final InfoService infoService = new MongoDbInfoService(COLLECTION);
		final ThreadService threadService = new MongoDbThreadService(COLLECTION);
		
		super.start();
		setDefaultResourceFilter(new ShareAndOwner());
		addController(new ActualitesController(COLLECTION, threadService, infoService));
	}

}
