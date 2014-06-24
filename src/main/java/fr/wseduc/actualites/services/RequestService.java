package fr.wseduc.actualites.services;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.ThreadResource;

public interface RequestService {

	public void ensureExtractUser(HttpServerRequest request, Handler<UserInfos> handler);
	
	public void ensureExtractThreadModel(HttpServerRequest request, Handler<ThreadResource> handler);
	
	public void ensureExtractInfoModel(HttpServerRequest request, Handler<InfoResource> handler);
}
