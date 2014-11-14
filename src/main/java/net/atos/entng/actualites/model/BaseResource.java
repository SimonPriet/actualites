package net.atos.entng.actualites.model;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

public interface BaseResource {

	public UserInfos getUser();
	
	public String getThreadId();
	
	public JsonObject getBody();
	
	
	public void setUser(UserInfos user) throws InvalidRequestException;
	
	public void setThreadId(String threadId) throws InvalidRequestException;
	
	public void setBody(JsonObject body) throws InvalidRequestException;
	
	
	public BaseResource requireUser();
	
	public BaseResource requireThreadId();
	
	public BaseResource requireBody();
}
