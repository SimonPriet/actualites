package fr.wseduc.actualites.model;

import java.util.Arrays;

public enum InfoMode {

	SUBMIT(0),
	DIRECT(1);
	
	private int id;
	
	private InfoMode(final int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static InfoState stateFromId(final int id) {
		for(InfoState state : Arrays.asList(InfoState.values())) {
			if (state.getId() == id) {
				return (InfoState.valueOf(state.toString()));
			}
		}
		return null;
	}
}
