package net.atos.entng.actualites.filters;

import net.atos.entng.actualites.model.InfoState;

public class StatePendingFilter extends AbstractStateFilter {

	public StatePendingFilter() {
		super(InfoState.PENDING);
	}
}
