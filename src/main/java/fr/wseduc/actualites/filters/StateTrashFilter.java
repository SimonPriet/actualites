package fr.wseduc.actualites.filters;

import fr.wseduc.actualites.model.InfoState;

public class StateTrashFilter extends AbstractStateFilter {

	public StateTrashFilter() {
		super(InfoState.TRASH);
	}
}
