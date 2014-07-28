package fr.wseduc.actualites.filters;

import java.util.HashMap;
import java.util.Map;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import fr.wseduc.actualites.model.InfoMode;
import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.webutils.http.Binding;

public class PublishFilter extends AbstractBaseFilter {

	@Override
	public void authorize(final HttpServerRequest request, final Binding binding, final UserInfos user,	final Handler<Boolean> handler) {
		try {
			final String threadId = ensureGetStringParameter(request, THREAD_ID_PARAMETER);
			final String infoId = ensureGetStringParameter(request, INFO_ID_PARAMETER);
			final String sharedMethod = binding.getServiceMethod().replaceAll("\\.", "-");
			
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
			// log.debug("Error in Filter : " + e.getMessage(), e);
			handler.handle(false);
		}
	}

}
