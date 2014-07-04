package fr.wseduc.actualites.model.impl;

import org.entcore.common.service.VisibilityFilter;
import org.entcore.common.user.UserInfos;

import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;

public class ThreadRequestModel extends AbstractRequestModel implements ThreadResource {
	
	private String threadId = null;
	private InfoState stateFilter = null;
	private VisibilityFilter visibilityFilter = null;
	
	public ThreadRequestModel(final UserInfos user, final String visibilityFilter) throws InvalidRequestException {
		super(user, false);
		
		try {
			this.visibilityFilter = VisibilityFilter.valueOf(visibilityFilter);
		}
		catch (Exception e) {
			this.visibilityFilter = VisibilityFilter.ALL;
		}
	}
	
	public ThreadRequestModel(final UserInfos user, final String visibilityFilter, final InfoState stateFilter) throws InvalidRequestException {
		this(user, visibilityFilter);
		this.stateFilter = stateFilter;
		if (stateFilter == null) {
			throw new InvalidRequestException("Invalid Parameters : StateFilter cannot be null");
		}
	}
	
	public ThreadRequestModel(final String threadId, final UserInfos user, final String visibilityFilter) throws InvalidRequestException {
		this(user, visibilityFilter);
		
		if (threadId == null) {
			throw new InvalidRequestException("Invalid Parameters : ThreadId cannot be null");
		}
		
		this.threadId = threadId;
	}
	
	public ThreadRequestModel(final String threadId, final UserInfos user, final String visibilityFilter, final InfoState stateFilter) throws InvalidRequestException {
		this(threadId, user, visibilityFilter);
		this.stateFilter = stateFilter;
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
