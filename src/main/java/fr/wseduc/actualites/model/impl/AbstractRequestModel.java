package fr.wseduc.actualites.model.impl;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InvalidRequestException;

public abstract class AbstractRequestModel {

	private final JsonObject body;
	private final UserInfos user;
	
	public AbstractRequestModel() {
		this.user = null;
		this.body = null;
	}
	
	public AbstractRequestModel(final UserInfos user) throws InvalidRequestException {
		if (user == null) {
			throw new InvalidRequestException("Missing User in Session");
		}
		this.user = user;
		this.body = null;
	}
	
	public AbstractRequestModel(final UserInfos user, final JsonObject body) throws InvalidRequestException {
		if (user == null) {
			throw new InvalidRequestException("Missing User in Session");
		}
		if (body == null) {
			throw new InvalidRequestException("Invalid Request Body");
		}
		this.user = user;
		this.body = body;
	}

	public JsonObject getBody() {
		return body;
	}

	public UserInfos getUser() {
		return user;
	}
}
