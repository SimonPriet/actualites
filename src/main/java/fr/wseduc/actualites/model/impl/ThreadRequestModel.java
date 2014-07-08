package fr.wseduc.actualites.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.entcore.common.service.VisibilityFilter;

import fr.wseduc.actualites.model.InfoState;
import fr.wseduc.actualites.model.InvalidRequestException;
import fr.wseduc.actualites.model.ThreadResource;
import fr.wseduc.actualites.model.ThreadStateVisibility;

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
