package net.atos.entng.actualites.filters;

import net.atos.entng.actualites.model.InfoState;

public class StatePublishedFilter extends AbstractStateFilter {

	public StatePublishedFilter() {
		super(InfoState.PUBLISHED);
	}
}
