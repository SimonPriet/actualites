package fr.wseduc.actualites.services;

import fr.wseduc.mongodb.MongoDb;

public class StateService extends AbstractService {

	public StateService(final MongoDb mongo, final String threadCollection, final String actualitesCollection) {
		super(mongo, threadCollection, actualitesCollection);
	}
}
