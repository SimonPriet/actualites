package fr.wseduc.actualites.services;

import fr.wseduc.mongodb.MongoDb;

public abstract class AbstractService {

	protected final MongoDb mongo;
	protected final String threadCollection;
	protected final String actualitesCollection;
	
	public AbstractService(final MongoDb mongo, final String threadCollection, final String actualitesCollection) {
		this.mongo = mongo;
		this.threadCollection = threadCollection;
		this.actualitesCollection = actualitesCollection;
	}

}
