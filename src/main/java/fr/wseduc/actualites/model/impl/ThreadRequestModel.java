package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;

public class ThreadRequestModel extends AbstractRequestModel implements ThreadResource {

	private final String ID_FIELD = "_id";
	
	private String threadId;
	private InfoState stateFilter;
	
	public ThreadRequestModel(UserInfos user, String threadId) throws InvalidRequestException {
		super(user);
		
		if (threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : ThreadId cannot be null");
		}
		
		this.threadId = threadId;
		this.stateFilter = null;
	}
	
	public ThreadRequestModel(UserInfos user, String threadId, String stateFilter) throws InvalidRequestException {
		this(user, threadId);
		this.stateFilter = InfoState.stateFromName(stateFilter);
		if (stateFilter == null) {
			throw new InvalidRequestException("Invalid Parameters : State cannot be null");
		}
	}
	
	public ThreadRequestModel(UserInfos user, JsonObject body) throws InvalidRequestException {
		super(user, body);
		try {
			this.threadId = getBody().getString(ID_FIELD);
		}
		catch(Exception e) {
			throw new InvalidRequestException(e);
		}
	}
	
	public void cleanPersistedObject() {
		// nothing to do
	}
	
	public String getThreadId() {
		return threadId;
	}
	
	public InfoState getStateFilter() {
		return stateFilter;
	}
}
