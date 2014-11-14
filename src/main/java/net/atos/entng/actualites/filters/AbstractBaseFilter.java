package net.atos.entng.actualites.filters;

import net.atos.entng.actualites.Actualites;
import net.atos.entng.actualites.model.InvalidRequestException;
import net.atos.entng.actualites.services.InfoService;
import net.atos.entng.actualites.services.impl.MongoDbInfoService;

import org.entcore.common.http.filter.ResourcesProvider;
import org.vertx.java.core.http.HttpServerRequest;

public abstract class AbstractBaseFilter implements ResourcesProvider {

	protected final String THREAD_ID_PARAMETER = "id";
	protected final String INFO_ID_PARAMETER = "infoid";
	
	protected final InfoService infoService;
	
	public AbstractBaseFilter() {
		this.infoService = new MongoDbInfoService(Actualites.COLLECTION);
	}
	
	protected String ensureGetStringParameter(final HttpServerRequest request, final String param) throws InvalidRequestException {
		String value = request.params().get(param);  
		if (value == null || value.trim().isEmpty()) {
			throw new InvalidRequestException("Missing parameter in request : " + param);
		}
		return value;
	}
}
