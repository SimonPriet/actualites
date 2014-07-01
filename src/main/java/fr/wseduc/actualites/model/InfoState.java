package fr.wseduc.actualites.model;

import java.util.Arrays;

public enum InfoState {

	DRAFT("draft", 1),
	PENDING("pending", 2),
	PUBLISHED("published", 3),
	TRASH("trash", 0);
	
	private String name;
	private int id;
	
	private InfoState(final String name, final int id) {
		this.setName(name);
		this.setId(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public static InfoState stateFromName(final String name) {
		for(InfoState state : Arrays.asList(InfoState.values())) {
			if (state.getName().equals(name)) {
				return (InfoState.valueOf(state.toString()));
			}
		}
		return null;
	}
}
