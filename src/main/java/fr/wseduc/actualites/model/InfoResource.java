package fr.wseduc.actualites.model;

public interface InfoResource extends RequestResource {
	
	public String getInfoId();

	public String getThreadId();

	public InfoState getState();
}
