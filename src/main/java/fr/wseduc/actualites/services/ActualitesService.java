package fr.wseduc.actualites.services;

import fr.wseduc.mongodb.MongoDb;

public class ActualitesService extends AbstractService {

	public ActualitesService(final MongoDb mongo, final String threadCollection, final String actualitesCollection) {
		super(mongo, threadCollection, actualitesCollection);
	}

}
