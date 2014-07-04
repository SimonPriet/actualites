package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;

public class InfoRequestModel extends AbstractRequestModel implements InfoResource {

	private final String ID_FIELD = "_id";
	private final String STATE_FIELD = "status";
	
	private String infoId;
	private String threadId;
	
	public InfoRequestModel(final String threadId, final String infoId) throws InvalidRequestException {
		super();
		
		if (infoId == null || threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : InfoId, ThreadId cannot be null");
		}
		
		this.infoId = infoId;
		this.threadId = threadId;
	}
	
	public InfoRequestModel(final UserInfos user, final String threadId, final String infoId) throws InvalidRequestException {
		super(user, true);
		
		if (infoId == null || threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : InfoId, ThreadId cannot be null");
		}
		
		this.infoId = infoId;
		this.threadId = threadId;
	}
	
	public InfoRequestModel(final UserInfos user, final String threadId, final JsonObject body) throws InvalidRequestException {
		super(user, true, body);
		
		if (threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : ThreadId cannot be null");
		}
		
		this.threadId = threadId;
	}
	
	public InfoRequestModel(final UserInfos user, final String threadId, final String infoId, final JsonObject body) throws InvalidRequestException {
		this(user, threadId, body);
		
		if (infoId == null) {
			throw new InvalidRequestException("Invalid Parameters : InfoId cannot be null");
		}
		
		this.infoId = infoId;
	}
	
	@Override
	public boolean isProtectedField(final String field) {
		return (ID_FIELD.equals(field) || STATE_FIELD.equals(field));
	}
	
	@Override
	public void cleanPersistedObject() {
		if (getBody().containsField(ID_FIELD)) {
			getBody().removeField(ID_FIELD);
		}
		if (getBody().containsField(STATE_FIELD)) {
			getBody().removeField(STATE_FIELD);
		}
	}

	@Override
	public String getInfoId() {
		return infoId;
	}

	@Override
	public String getThreadId() {
		return threadId;
	}
}
