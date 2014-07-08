package fr.wseduc.actualites.model.impl;

import fr.wseduc.actualites.model.InfoResource;
import fr.wseduc.actualites.model.InvalidRequestException;

public class InfoRequestModel extends AbstractRequestModel implements InfoResource {

	private final String ID_FIELD = "_id";
	private final String STATE_FIELD = "status";
	private final String COMMENTS_FIELD = "comments";
	
	private String infoId;
	
	private boolean requireInfoId = false;
	
	
	@Override
	public String getInfoId() {
		return infoId;
	}
	
	
	@Override
	public void setInfoId(String infoId) throws InvalidRequestException {
		if (requireInfoId && (infoId == null || infoId.trim().isEmpty())) {
			throw new InvalidRequestException("Invalid Parameters : InfoId cannot be null");
		}
		this.infoId = infoId;
	}
	
	
	@Override
	public InfoResource requireInfoId() {
		this.requireInfoId = true;
		return this;
	}
	
	
	@Override
	public boolean isProtectedField(final String field) {
		return (ID_FIELD.equals(field) || STATE_FIELD.equals(field) || COMMENTS_FIELD.equals(field));
	}
	
	@Override
	public void cleanPersistedObject() {
		if (getBody().containsField(ID_FIELD)) {
			getBody().removeField(ID_FIELD);
		}
		if (getBody().containsField(STATE_FIELD)) {
			getBody().removeField(STATE_FIELD);
		}
		if (getBody().containsField(COMMENTS_FIELD)) {
			getBody().removeField(COMMENTS_FIELD);
		}
	}

	
}
