package net.atos.entng.actualites;

import net.atos.entng.actualites.controllers.ActualitesController;
import net.atos.entng.actualites.services.InfoService;
import net.atos.entng.actualites.services.ThreadService;
import net.atos.entng.actualites.services.impl.MongoDbInfoService;
import net.atos.entng.actualites.services.impl.MongoDbThreadService;

import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;

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
