package fr.wseduc.actualites.filters;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.http.Binding;

public interface ResourcesProvider {

	void authorize(HttpServerRequest resourceRequest, Binding binding,
				   JsonObject user, Handler<Boolean> handler);

}
