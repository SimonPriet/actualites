package fr.wseduc.actualites.filters;

import fr.wseduc.actualites.model.InfoState;

public class StateDraftFilter extends AbstractStateFilter {

	public StateDraftFilter() {
		super(InfoState.DRAFT);
	}
}