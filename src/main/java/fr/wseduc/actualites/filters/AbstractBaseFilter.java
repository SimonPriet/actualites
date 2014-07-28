package fr.wseduc.actualites.filters;

import org.entcore.common.http.filter.ResourcesProvider;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.Actualites;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.impl.MongoDbInfoService;

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
