package fr.wseduc.actualites.model;

import java.util.List;

import org.entcore.common.service.VisibilityFilter;

public interface ThreadResource extends BaseResource {
	
	public VisibilityFilter getVisibilityFilter();
	
	public InfoState getStateFilter();
	
	
	public void setVisibilityFilter(String visibilityFilter, VisibilityFilter defaultVisibilityFilter) throws InvalidRequestException;
	
	public void setStateFilter(String stateFilter) throws InvalidRequestException;
	
	public void setStateFilter(InfoState state) throws InvalidRequestException;
	
	public void setVisibilityFilter(VisibilityFilter visibilityFilter);
	
	
	public ThreadResource requireVisibiliyFilter();
	
	public ThreadResource requireStateFilter();
	
	
	public boolean hasStateFilter();
}
