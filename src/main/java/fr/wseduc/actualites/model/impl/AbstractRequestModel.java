package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.BaseResource;
import fr.wseduc.actualites.model.InvalidRequestException;

public abstract class AbstractRequestModel implements BaseResource {
	
	private UserInfos user;
	private String threadId;
	private JsonObject body;
	
	private boolean requireUser = false;
	private boolean requireThreadId = false;
	private boolean requireBody = false;
	

	@Override
	public UserInfos getUser() {
		return user;
	}
	
	@Override
	public String getThreadId() {
		return threadId;
	}
	
	@Override
	public JsonObject getBody() {
		return body;
	}
	
	
	@Override
	public void setUser(UserInfos user) throws InvalidRequestException {
		if (requireUser && user == null) {
			throw new InvalidRequestException("Invalid Request : Missing User in Session");
		}
		this.user = user;
	}
	
	@Override
	public void setThreadId(String threadId) throws InvalidRequestException {
		if (requireThreadId && (threadId == null || threadId.trim().isEmpty())) {
			throw new InvalidRequestException("Invalid Parameters : ThreadId cannot be null");
		}
		this.threadId = threadId;
	}
	
	@Override
	public void setBody(JsonObject body) throws InvalidRequestException {
		if (requireBody && body == null) {
			throw new InvalidRequestException("Invalid Request : Request body is null");
		}
	}
	
	
	@Override
	public BaseResource requireUser() {
		this.requireUser = true;
		return this;
	}
	
	@Override
	public BaseResource requireThreadId() {
		this.requireThreadId = true;
		return this;
	}
	
	@Override
	public BaseResource requireBody() {
		this.requireBody = true;
		return this;
	}
}
