package fr.wseduc.actualites.services;

import fr.wseduc.mongodb.MongoDb;

public class ThreadService extends AbstractService {

	public ThreadService(final MongoDb mongo, final String threadCollection, final String actualitesCollection) {
		super(mongo, threadCollection, actualitesCollection);
	}
}
