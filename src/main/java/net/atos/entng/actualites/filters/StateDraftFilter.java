package net.atos.entng.actualites.filters;

import net.atos.entng.actualites.model.InfoState;

public class StateDraftFilter extends AbstractStateFilter {

	public StateDraftFilter() {
		super(InfoState.DRAFT);
	}
}