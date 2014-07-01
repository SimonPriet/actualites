package fr.wseduc.actualites.model.impl;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;

import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;

public class ThreadRequestModel extends AbstractRequestModel implements ThreadResource {
	
	private String threadId;
	private InfoState stateFilter = null;
	private VisibilityFilter visibilityFilter = null;
	
	public ThreadRequestModel(final UserInfos user, final String threadId, final String visibilityFilter) throws InvalidRequestException {
		super(user);
		
		try {
			this.visibilityFilter = VisibilityFilter.valueOf(visibilityFilter);
		}
		catch (Exception e) {
			this.visibilityFilter = VisibilityFilter.ALL;
		}
		
		if (threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : ThreadId cannot be null");
		}
		
		this.threadId = threadId;
	}
	
	public ThreadRequestModel(final UserInfos user, final String threadId, final String visibilityFilter, final String stateFilter) throws InvalidRequestException {
		this(user, threadId, visibilityFilter);
		this.stateFilter = InfoState.stateFromName(stateFilter);
		if (stateFilter == null) {
			throw new InvalidRequestException("Invalid Parameters : StateFilter cannot be null");
		}
	}
	
	@Override
	public String getThreadId() {
		return threadId;
	}
	
	@Override
	public InfoState getStateFilter() {
		return stateFilter;
	}

	@Override
	public VisibilityFilter getVisibilityFilter() {
		return visibilityFilter;
	}
}
