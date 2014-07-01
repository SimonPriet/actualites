package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;

public class StateControllerHelper extends BaseExtractorHelper {
	
	protected final InfoService infoService;
	
	public StateControllerHelper(final InfoService infoService) {
		this.infoService = infoService;
	}
	
	@Put("/info/:id/submit")
	@ApiDoc("Submit : Change an Info to Pending state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void submit(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.PENDING, notEmptyResponseHandler(request));
			}
		});
	}
	
	@Put("/info/:id/unsubmit")
	@ApiDoc("Cancel Submit : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.contribute", type = ActionType.RESOURCE)
	public void unsubmit(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
			}
		});
	}
	
	@Put("/info/:id/publish")
	@ApiDoc("Publish : Change an Info to Published state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void publish(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.PUBLISHED, notEmptyResponseHandler(request));
			}
		});
	}
	
	@Put("/info/:id/unpublish")
	@ApiDoc("Unpublish : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.publish", type = ActionType.RESOURCE)
	public void unpublish(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
			}
		});
	}
	
	@Put("/info/:id/thrash")
	@ApiDoc("Trash : Change an Info to Trash state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void trash(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.TRASH, notEmptyResponseHandler(request));
			}
		});
	}
	
	@Put("/info/:id/restore")
	@ApiDoc("Cancel Trash : Change an Info to Draft state in thread by thread and by id")
	@SecuredAction(value = "thread.manage", type = ActionType.RESOURCE)
	public void restore(final HttpServerRequest request) {
		ensureExtractInfoFromRequestParameters(request, new Handler<InfoResource>(){
			@Override
			public void handle(InfoResource info) {
				infoService.changeState(info, InfoState.DRAFT, notEmptyResponseHandler(request));
			}
		});
	}
}
