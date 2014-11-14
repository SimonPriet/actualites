package net.atos.entng.actualites.filters;

import net.atos.entng.actualites.model.InfoState;

public class StateTrashFilter extends AbstractStateFilter {

	public StateTrashFilter() {
		super(InfoState.TRASH);
	}
}
