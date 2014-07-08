package fr.wseduc.actualites.filters;

import java.util.HashMap;
import java.util.Map;

import org.entcore.common.http.filter.MongoAppFilter;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoMode;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.services.InfoService;

public class ActualitesFilter extends MongoAppFilter {

	private final String THREAD_ID_PARAMETER = "id";
	private final String INFO_ID_PARAMETER = "infoid";
	
	protected final InfoService infoService;
	
	public ActualitesFilter(String collection, final InfoService infoService) {
		super(collection);
		this.infoService = infoService;
	}
	
	public void stateDraft(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		stateCheck(InfoState.DRAFT, request, sharedMethod, user, handler);
	}
	
	public void statePending(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		stateCheck(InfoState.PENDING, request, sharedMethod, user, handler);
	}
	
	public void statePublished(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		stateCheck(InfoState.PUBLISHED, request, sharedMethod, user, handler);
	}
	
	public void publish(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		try {
			String threadId = ensureGetStringParameter(request, THREAD_ID_PARAMETER);
			String infoId = ensureGetStringParameter(request, INFO_ID_PARAMETER);
			
			Map<InfoMode, InfoState> statesAndModes = new HashMap<InfoMode, InfoState>(2);
			statesAndModes.put(InfoMode.SUBMIT, InfoState.PENDING);
			statesAndModes.put(InfoMode.DIRECT, InfoState.DRAFT);
			
			request.pause();
			infoService.canDoByStatesAndModes(user, threadId, infoId, sharedMethod, statesAndModes, new Handler<Boolean>(){
				@Override
				public void handle(Boolean event) {
					request.resume();
					handler.handle(event);
				}
			});
		}
		catch (Exception e) {
			log.debug("Error in Filter : " + e.getMessage(), e);
			handler.handle(false);
		}
	}
	
	public void trashMine(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		stateCheck(InfoState.DRAFT, request, sharedMethod, user, handler);
	}
	
	public void restoreMine(final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		stateCheck(InfoState.TRASH, request, sharedMethod, user, handler);
	} 
	
	protected void stateCheck(final InfoState state, final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		try {
			String threadId = ensureGetStringParameter(request, THREAD_ID_PARAMETER);
			String infoId = ensureGetStringParameter(request, INFO_ID_PARAMETER);
			
			request.pause();
			infoService.canDoByState(user, threadId, infoId, sharedMethod, state, new Handler<Boolean>(){
				@Override
				public void handle(Boolean event) {
					request.resume();
					handler.handle(event);
				}
			});
		}
		catch (Exception e) {
			log.debug("Error in Filter : " + e.getMessage(), e);
			handler.handle(false);
		}
	}
	
	protected void stateCheckAndMine(final InfoState state, final HttpServerRequest request, final String sharedMethod, final UserInfos user, final Handler<Boolean> handler) {
		try {
			String threadId = ensureGetStringParameter(request, THREAD_ID_PARAMETER);
			String infoId = ensureGetStringParameter(request, INFO_ID_PARAMETER);
			
			request.pause();
			infoService.canDoMineByState(user, threadId, infoId, sharedMethod, state, new Handler<Boolean>(){
				@Override
				public void handle(Boolean event) {
					request.resume();
					handler.handle(event);
				}
			});
		}
		catch (Exception e) {
			log.debug("Error in Filter : " + e.getMessage(), e);
			handler.handle(false);
		}
	}
	
	protected String ensureGetStringParameter(final HttpServerRequest request, final String param) throws InvalidRequestException {
		String value = request.params().get(param);  
		if (value == null || value.trim().isEmpty()) {
			throw new InvalidRequestException("Missing parameter in request : " + param);
		}
		return value;
	}
}
