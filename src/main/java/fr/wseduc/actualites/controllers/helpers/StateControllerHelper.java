package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.webutils.http.Renders;

public class StateControllerHelper extends BaseExtractorHelper {
	
	private final String THREAD_ID_PARAMETER = "id";
	private final String INFO_ID_PARAMETER = "infoid";
	
	protected final InfoService infoService;
	
	public StateControllerHelper(final InfoService infoService) {
		this.infoService = infoService;
	}
	
	public void submit(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.PENDING, notEmptyResponseHandler(request));
	}
	
	public void unsubmit(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
	}
	
	public void publish(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.PUBLISHED, notEmptyResponseHandler(request));
	}
	
	public void unpublish(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
	}
	
	public void trash(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.TRASH, notEmptyResponseHandler(request));
	}
	
	public void restore(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		
		infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
	}
	
	protected void extractInfoId(final HttpServerRequest request, final InfoResource info) {
		try {
			info.setInfoId(request.params().get(INFO_ID_PARAMETER));
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
	}
}
