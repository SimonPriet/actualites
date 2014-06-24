package fr.wseduc.actualites.model;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

public interface RequestResource {
	
	public JsonObject getBody();

	public UserInfos getUser();
}
