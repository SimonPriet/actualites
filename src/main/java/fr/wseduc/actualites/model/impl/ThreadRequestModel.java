package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;

public class ThreadRequestModel extends AbstractRequestModel implements ThreadResource {

	private final String ID_FIELD = "_id";
	
	private String threadId;
	
	public ThreadRequestModel(UserInfos user, JsonObject body) throws InvalidRequestException {
		super(user, body);
		try {
			this.threadId = getBody().getString(ID_FIELD);
		}
		catch(Exception e) {
			throw new InvalidRequestException(e);
		}
	}
	
	public String getThreadId() {
		return threadId;
	}
}
