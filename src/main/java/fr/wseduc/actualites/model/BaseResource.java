package fr.wseduc.actualites.model;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

public interface BaseResource {
	
	public JsonObject getBody();

	public UserInfos getUser();
	
	public void cleanPersistedObject();
}
