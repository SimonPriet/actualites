package fr.wseduc.actualites.filters;

import fr.wseduc.actualites.model.InfoState;

public class StatePublishedFilter extends AbstractStateFilter {

	public StatePublishedFilter() {
		super(InfoState.PUBLISHED);
	}
}
