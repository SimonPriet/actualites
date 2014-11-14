package net.atos.entng.actualites.model;


public interface InfoResource extends BaseResource {
	
	public String getInfoId();
	
	
	public void setInfoId(String infoId) throws InvalidRequestException;
	
	
	public InfoResource requireInfoId();
	
	
	public void cleanPersistedObject();
	
	public boolean isProtectedField(String field);
}
