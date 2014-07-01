package fr.wseduc.actualites.model;

public interface InfoResource extends BaseResource {
	
	public String getInfoId();

	public String getThreadId();

	public InfoState getState();
	
	public boolean isProtectedField(String field);
}
