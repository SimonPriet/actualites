package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;

public class InfoRequestModel extends AbstractRequestModel implements InfoResource {

	private final String ID_FIELD = "_id";
	private final String THREAD_FIELD = "thread";
	private final String STATE_FIELD = "status";
	
	private String infoId;
	private String threadId;
	private InfoState state;
	
	public InfoRequestModel(UserInfos user, String threadId, String infoId, InfoState state) throws InvalidRequestException {
		super(user);
	
		if (infoId == null || threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : InfoId, ThreadId cannot be null");
		}
		
		this.infoId = infoId;
		this.threadId = threadId;
		this.state = state;
	}
	
	public InfoRequestModel(UserInfos user, JsonObject body) throws InvalidRequestException {
		super(user, body);
		try {
			this.infoId = getBody().getString(ID_FIELD);
			this.threadId = getBody().getString(THREAD_FIELD);
			this.state = InfoState.stateFromId(getBody().getInteger(STATE_FIELD));
		}
		catch(Exception e) {
			throw new InvalidRequestException(e);
		}
		
		if (this.state == null) {
			throw new InvalidRequestException("Invalid Info State");
		}
	}
	
	public boolean isProtectedField(final String field) {
		return (ID_FIELD.equals(field) || THREAD_FIELD.equals(field) || STATE_FIELD.equals(field));
	}
	
	public void cleanPersistedObject() {
		getBody().removeField(THREAD_FIELD);
	}

	public String getInfoId() {
		return infoId;
	}

	public String getThreadId() {
		return threadId;
	}

	public InfoState getState() {
		return state;
	}
}
