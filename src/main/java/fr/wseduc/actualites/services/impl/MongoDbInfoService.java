package fr.wseduc.actualites.services.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.webutils.Either;

public class MongoDbInfoService extends AbstractService implements InfoService {

	public MongoDbInfoService(final String threadCollection, final String infosCollection) {
		super(threadCollection, infosCollection);
	}

	@Override
	public void create(final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retrieve(final String id, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(final String id, final InfoResource info, final Handler<Either<String, JsonObject>> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(final String id, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void list(final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		list(null, user, handler);
	}
	
	@Override
	public void list(final InfoState state, final UserInfos user, final Handler<Either<String, JsonArray>> handler) {
		// TODO Auto-generated method stub
		
	}


}
