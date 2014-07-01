package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.webutils.http.Renders;

public class InfoControllerHelper extends BaseExtractorHelper {
	
	protected final InfoService infoService;
	
	public InfoControllerHelper(final InfoService infoService) {
		this.infoService = infoService;
	}
	
	public void getThreadActualites(final HttpServerRequest request) {
		ensureExtractThreadFromRequestParameters(request, new Handler<ThreadResource>(){
			@Override
			public void handle(ThreadResource thread) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void getThreadActualitesByStatus(final HttpServerRequest request) {
		ensureExtractThreadFromRequestParameters(request, new Handler<ThreadResource>(){
			@Override
			public void handle(ThreadResource thread) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void createDraft(final HttpServerRequest request) {
		ensureExtractInfoFromRequestBody(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.DRAFT) {
					infoService.create(info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void updateDraft(final HttpServerRequest request) {
		ensureExtractInfoFromRequestBody(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.DRAFT) {
					infoService.update(info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void updatePending(final HttpServerRequest request) {
		ensureExtractInfoFromRequestBody(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.PENDING) {
					infoService.update(info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void updatePublished(final HttpServerRequest request) {
		ensureExtractInfoFromRequestBody(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.PUBLISHED) {
					infoService.update(info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void delete(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.delete(info, notEmptyResponseHandler(request));
			}
		});
	}
}
