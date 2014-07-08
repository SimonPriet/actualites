package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.service.VisibilityFilter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.BaseResource;
import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
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
	
	public void listPublicInfos(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		
		extractVisibiltyFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				try {
					infoService.listPublic(thread, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
	
	public void listThreadPublicInfos(final HttpServerRequest request) {
		final ThreadResource thread = new ThreadRequestModel();
		thread.requireUser();
		thread.requireThreadId();
		
		extractThreadId(request, thread);
		extractVisibiltyFilter(request, thread);
		extractUserFromRequest(thread, request, new Handler<BaseResource>() {
			@Override
			public void handle(final BaseResource model) {
				try {
					infoService.listPublic(thread, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
				try {
					infoService.list(thread, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
				try {
					infoService.list(thread, arrayResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
			}
		});
	}
	
	public void retrieve(final HttpServerRequest request) {
		final InfoResource info = new InfoRequestModel();
		info.requireThreadId();
		info.requireInfoId();
		
		extractThreadId(request, info);
		extractInfoId(request, info);
		try {
			infoService.retrieve(info, notEmptyResponseHandler(request));
		}
		catch (Exception e) {
			renderErrorException(request, e);
		}
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
				try {
					infoService.create(info, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
				try {
					infoService.update(info, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
				try {
					infoService.delete(info, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
				try {
					infoService.addComment(info, notEmptyResponseHandler(request));
				}
				catch (Exception e) {
					renderErrorException(request, e);
				}
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
