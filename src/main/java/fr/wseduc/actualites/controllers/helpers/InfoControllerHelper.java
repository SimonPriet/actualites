package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import java.util.Map;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.actualites.services.impl.MongoDbInfoService;
import fr.wseduc.webutils.http.BaseController;
import fr.wseduc.webutils.http.Renders;

public class InfoControllerHelper extends BaseController {
	
	protected final InfoService infoService;
	
	public InfoControllerHelper(final String threadCollection, final String infoCollection) {
		this.infoService = new MongoDbInfoService(threadCollection, infoCollection);
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		((MongoDbInfoService) this.infoService).init(vertx, container, rm, securedActions);
	}
	
	public void getThreadActualites(final HttpServerRequest request) {
		infoService.ensureExtractThreadModel(request, new Handler<ThreadResource>(){
			@Override
			public void handle(ThreadResource thread) {
				infoService.list(thread.getUser(), arrayResponseHandler(request));
			}
		});
	}
	
	public void getThreadActualitesByStatus(final HttpServerRequest request) {
		infoService.ensureExtractThreadModel(request, new Handler<ThreadResource>(){
			@Override
			public void handle(ThreadResource thread) {
				final InfoState state = InfoState.stateFromName(request.params().get("status"));
				if (state != null) {
					infoService.list(thread.getUser(), arrayResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid State Filter : State not resolved");
				}
			}
		});
	}
	
	public void createDraft(final HttpServerRequest request) {
		infoService.ensureExtractInfoModel(request, new Handler<InfoResource>(){
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
		infoService.ensureExtractInfoModel(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.DRAFT) {
					infoService.update(request.params().get("id"), info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void updatePending(final HttpServerRequest request) {
		infoService.ensureExtractInfoModel(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.PENDING) {
					infoService.update(request.params().get("id"), info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void updatePublished(final HttpServerRequest request) {
		infoService.ensureExtractInfoModel(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				if (info.getState() == InfoState.PUBLISHED) {
					infoService.update(request.params().get("id"), info, notEmptyResponseHandler(request));
				}
				else {
					Renders.unauthorized(request, "Invalid Info State : Can only create Info in Draft State");
				}
			}
		});
	}
	
	public void delete(final HttpServerRequest request) {
		infoService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				infoService.delete(request.params().get("id"), user, notEmptyResponseHandler(request));
			}
		});
	}
}
