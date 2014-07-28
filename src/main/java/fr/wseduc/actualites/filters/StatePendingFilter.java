package fr.wseduc.actualites.filters;

import fr.wseduc.actualites.model.InfoState;

public class StatePendingFilter extends AbstractStateFilter {

	public StatePendingFilter() {
		super(InfoState.PENDING);
	}
}
