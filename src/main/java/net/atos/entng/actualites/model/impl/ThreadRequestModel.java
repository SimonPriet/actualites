package net.atos.entng.actualites.model.impl;

import net.atos.entng.actualites.model.InfoState;
import net.atos.entng.actualites.model.InvalidRequestException;
import net.atos.entng.actualites.model.ThreadResource;

import org.entcore.common.service.VisibilityFilter;

public class ThreadRequestModel extends AbstractRequestModel implements ThreadResource {
	
	private InfoState stateFilter;
	private VisibilityFilter visibilityFilter;
	
	private boolean requireStateFilter = false;
	private boolean requireVisibilityFilter = false;
	
	
	@Override
	public InfoState getStateFilter() {
		return stateFilter;
	}

	@Override
	public VisibilityFilter getVisibilityFilter() {
		return visibilityFilter;
	}
	
	
	@Override
	public void setVisibilityFilter(final String visibilityFilter, final VisibilityFilter defaultVisibilityFilter) throws InvalidRequestException {
		if (requireVisibilityFilter && visibilityFilter == null) {
			throw new InvalidRequestException("Invalid Parameters : Visibility Filter cannot be null");
		}
		
		try {
			this.visibilityFilter = VisibilityFilter.valueOf(visibilityFilter);
		}
		catch (Exception e) {
			this.visibilityFilter = defaultVisibilityFilter;
		}
	}
	
	@Override
	public void setVisibilityFilter(final VisibilityFilter visibilityFilter) {
		this.visibilityFilter = visibilityFilter;
	}
	
	
	@Override
	public void setStateFilter(String stateFilter) throws InvalidRequestException {
		this.stateFilter = InfoState.stateFromName(stateFilter);
		if (requireStateFilter && this.stateFilter == null) {
			throw new InvalidRequestException("Invalid Parameters : Invalid or null State filter");
		}
	}
	
	@Override
	public void setStateFilter(InfoState state) throws InvalidRequestException {
		if (requireStateFilter && state == null) {
			throw new InvalidRequestException("Invalid Parameters : Invalid State filter");
		}
		this.stateFilter = state;
	}
	
	
	@Override
	public ThreadResource requireVisibiliyFilter() {
		this.requireVisibilityFilter = true;
		return this;
	}
	
	@Override
	public ThreadResource requireStateFilter()  {
		this.requireStateFilter = true;
		return this;
	}
	
	
	@Override
	public boolean hasStateFilter() {
		return (this.stateFilter != null);
	}
}
