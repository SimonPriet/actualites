package fr.wseduc.actualites.controllers.helpers;

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
import fr.wseduc.actualites.services.StateService;
import fr.wseduc.actualites.services.impl.MongoDbStateService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;

public class StateControllerHelper extends BaseController {
	
	protected final StateService stateService;
	
	public StateControllerHelper(final String threadCollection, final String infoCollection) {
		this.stateService = (StateService) new MongoDbStateService(threadCollection, infoCollection);
	}
	
	@Override
	public void init(Vertx vertx, Container container, RouteMatcher rm, Map<String, fr.wseduc.webutils.security.SecuredAction> securedActions) {
		super.init(vertx, container, rm, securedActions);
		((MongoDbStateService) this.stateService).init(vertx, container, rm, securedActions);
	}
	
	@Put("/info/:id/submit")
	@ApiDoc("Submit : Change an Info to Pending state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void submit(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.DRAFT) {
							stateService.changeState(info, InfoState.PENDING, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Submit : Can only change state from DRAFT To PENDING");
						}
					}
				});
			}
		});
	}
	
	@Put("/info/:id/unsubmit")
	@ApiDoc("Cancel Submit : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void unsubmit(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.PENDING) {
							stateService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Unsubmit : Can only change state from PENDING To DRAFT");
						}
					}
				});
			}
		});
	}
	
	@Put("/info/:id/publish")
	@ApiDoc("Publish : Change an Info to Published state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void publish(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.PENDING) {
							stateService.changeState(info, InfoState.PUBLISHED, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Publish : Can only change state from PENDING To PUBLISHED");
						}
					}
				});
			}
		});
	}
	
	@Put("/info/:id/unpublish")
	@ApiDoc("Unpublish : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void unpublish(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.PUBLISHED) {
							stateService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Unpublish : Can only change state from PUBLISHED To DRAFT");
						}
					}
				});
			}
		});
	}
	
	@Put("/info/:id/thrash")
	@ApiDoc("Trash : Change an Info to Trash state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void trash(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.DRAFT) {
							stateService.changeState(info, InfoState.TRASH, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Trash : Can only change state from DRAFT To TRASH");
						}
					}
				});
			}
		});
	}
	
	@Put("/info/:id/restore")
	@ApiDoc("Cancel Trash : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void restore(final HttpServerRequest request) {
		stateService.ensureExtractUser(request, new Handler<UserInfos>(){
			@Override
			public void handle(UserInfos user) {
				stateService.retrieveState(request.params().get("id"), user, new Handler<InfoResource>(){
					@Override
					public void handle(InfoResource info) {
						if (info.getState() == InfoState.TRASH) {
							stateService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
						}
						else {
							unauthorized(request, "Restore : Can only change state from TRASH To DRAFT");
						}
					}
				});
			}
		});
	}
}
