package net.atos.entng.actualites.model;

public class InvalidRequestException extends Exception {

	private static final long serialVersionUID = -1276842449768390969L;

	public InvalidRequestException(String message) {
		super(message);
	}
	
	public InvalidRequestException(Exception e) {
		super(e);
	}
	
	public InvalidRequestException(String message, Exception e) {
		super(message, e);
	}
}
