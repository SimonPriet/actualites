package fr.wseduc.actualites.model;

import org.entcore.common.service.VisibilityFilter;

public interface ThreadResource extends BaseResource {

	public String getThreadId();
	
	public VisibilityFilter getVisibilityFilter();
	
	public InfoState getStateFilter();
}
