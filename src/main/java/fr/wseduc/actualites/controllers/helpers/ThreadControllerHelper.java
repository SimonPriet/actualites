package fr.wseduc.actualites.controllers.helpers;

import org.vertx.java.core.http.HttpServerRequest;

public class ThreadControllerHelper extends ManageableResourcesControllerHelper {
	protected final String threadCollection;
	
	public ThreadControllerHelper(final String threadCollection) {
		super(threadCollection);
		
		this.threadCollection = threadCollection;
	}
	
	public void listThreads(final HttpServerRequest request) {
		super.list(request);
	}
	
	public void createThread(final HttpServerRequest request) {
		super.create(request);
	}
	
	public void getThread(final HttpServerRequest request) {
		super.retrieve(request);
	}

	public void updateThread(final HttpServerRequest request) {
		super.update(request);
	}
	
	public void deleteThread(final HttpServerRequest request) {
		super.delete(request);
	}
	
	public void shareThread(final HttpServerRequest request) {
		super.shareJson(request);
	}
	
	public void shareThreadSubmit(final HttpServerRequest request) {
		super.shareJsonSubmit(request, null);
	}
	
	public void shareThreadRemove(final HttpServerRequest request) {
		super.removeShare(request);
	}
}
