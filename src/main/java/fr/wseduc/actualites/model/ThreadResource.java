package fr.wseduc.actualites.model;

public interface ThreadResource extends BaseResource {

	public String getThreadId();
	
	public InfoState getStateFilter();
}
