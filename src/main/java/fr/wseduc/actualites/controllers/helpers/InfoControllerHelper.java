package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.service.VisibilityFilter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.BaseResource;
import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.model.impl.ThreadRequestModel;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.webutils.http.Renders;

public class InfoControllerHelper extends BaseExtractorHelper {
	
	private static final String INFO_ID_PARAMETER = "infoid";
	private static final String STATE_PARAMETER = "status";
	private static final String VISIBILITY_FILTER_PARAMETER = "filter";
	
	private static final VisibilityFilter DEFAULT_VISIBILITY_FILTER = VisibilityFilter.OWNER_AND_SHARED;
	
	protected final InfoService infoService;
	
	public InfoControllerHelper(final InfoService infoService) {
		this.infoService = infoService;
	}
	
	public void listInfos(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		
		extractVisibiltyFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void listInfosByStatus(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		thread.requireStateFilter();
		
		extractVisibiltyFilter(request, thread);
		extractStateFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void listThreadInfos(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		thread.requireThreadId();
		
		extractThreadId(request, thread);
		extractVisibiltyFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void listThreadInfosByStatus(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		thread.requireThreadId();
		thread.requireStateFilter();
		
		extractThreadId(request, thread);
		extractVisibiltyFilter(request, thread);
		extractStateFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.list(thread, arrayResponseHandler(request));
			}
		});
	}
	
	public void create(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireUser();
		info.requireThreadId();
		info.requireBody();
		
		extractThreadId(request, info);
		extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.create(info, notEmptyResponseHandler(request));
			}
		});
	}
	
	public void update(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireUser();
		info.requireThreadId();
		info.requireInfoId();
		info.requireBody();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.update(info, notEmptyResponseHandler(request));
			}
		});
	}
	
	public void delete(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireUser();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		extractUserFromRequest(info, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.delete(info, notEmptyResponseHandler(request));
			}
		});
	}
	
	public void comment(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireUser();
		info.requireThreadId();
		info.requireInfoId();
		info.requireBody();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		extractUserAndBodyFromRequest(info, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				infoService.addComment(info, notEmptyResponseHandler(request));
			}
		});
	}
	
	
	protected void extractVisibiltyFilter(final HttpServerRequest request, final ThreadResource thread) {
		try {
			thread.setVisibilityFilter(request.params().get(VISIBILITY_FILTER_PARAMETER), DEFAULT_VISIBILITY_FILTER);
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
	}
	
	protected void extractStateFilter(final HttpServerRequest request, final ThreadResource thread) {
		try {
			thread.setStateFilter(request.params().get(STATE_PARAMETER));
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
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
