package fr.wseduc.actualites.controllers.helpers;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.model.impl.InfoRequestModel;
import fr.wseduc.actualites.model.impl.ThreadRequestModel;
import fr.wseduc.actualites.services.InfoService;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;

public class InfoControllerHelper extends BaseExtractorHelper {
	
	private final String THREAD_ID_PARAMETER = "id";
	private final String INFO_ID_PARAMETER = "infoid";
	private final String STATE_PARAMETER = "status";
	private final String VISIBILITY_FILTER_PARAMETER = "filter";
	
	protected final InfoService infoService;
	
	public InfoControllerHelper(final InfoService infoService) {
		this.infoService = infoService;
	}
	
	public void getThreadActualites(final HttpServerRequest request) {
		// User id mandatory
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				try {
					ThreadResource thread = new ThreadRequestModel(
							user,
							request.params().get(THREAD_ID_PARAMETER),
							request.params().get(VISIBILITY_FILTER_PARAMETER)
						);
					infoService.list(thread, arrayResponseHandler(request));
				}
				catch (InvalidRequestException ire) {
					log.debug("Invalid request : " + ire.getMessage());
					Renders.badRequest(request, ire.getMessage());
				}
			}
		});
	}
	
	public void getThreadActualitesByStatus(final HttpServerRequest request) {
		// User is mandatory
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				try {
					ThreadResource thread = new ThreadRequestModel(
							user,
							request.params().get(THREAD_ID_PARAMETER),
							request.params().get(VISIBILITY_FILTER_PARAMETER),
							request.params().get(STATE_PARAMETER)
						);
					infoService.list(thread, arrayResponseHandler(request));
				}
				catch (InvalidRequestException ire) {
					log.debug("Invalid request : " + ire.getMessage());
					Renders.badRequest(request, ire.getMessage());
				}
			}
		});
	}
	
	public void create(final HttpServerRequest request) {
		// User is mandatory
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject object) {
						try {
							InfoResource info = new InfoRequestModel(
									user,
									request.params().get(THREAD_ID_PARAMETER),
									object
								);
							infoService.create(info, notEmptyResponseHandler(request));
						}
						catch (InvalidRequestException ire) {
							log.debug("Invalid request : " + ire.getMessage());
							Renders.badRequest(request, ire.getMessage());
						}
					}
				});
			}
		});
	}
	
	public void update(final HttpServerRequest request) {
		ensureExtractUserFromRequest(request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
					@Override
					public void handle(JsonObject object) {
						try {
							InfoResource info = new InfoRequestModel(
									user,
									request.params().get(THREAD_ID_PARAMETER),
									request.params().get(INFO_ID_PARAMETER),
									object
								);
							infoService.update(info, notEmptyResponseHandler(request));
						}
						catch (InvalidRequestException ire) {
							log.debug("Invalid request : " + ire.getMessage());
							Renders.badRequest(request, ire.getMessage());
						}
					}
				});
			}
		});
	}
	
	public void delete(final HttpServerRequest request) {
		try {
			InfoResource info = new InfoRequestModel(
					request.params().get(THREAD_ID_PARAMETER),
					request.params().get(INFO_ID_PARAMETER)
				);
			infoService.delete(info, notEmptyResponseHandler(request));
		}
		catch (InvalidRequestException ire) {
			log.debug("Invalid request : " + ire.getMessage());
			Renders.badRequest(request, ire.getMessage());
		}
	}
}
