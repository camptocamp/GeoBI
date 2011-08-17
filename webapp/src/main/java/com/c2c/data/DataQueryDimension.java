package com.c2c.data;

import java.util.ArrayList;
import java.util.List;

public class DataQueryDimension {
	
	private String uniqueName;
	private List<DataQueryLevel> levels;
	
	public DataQueryDimension(String uniqueName) {
		this.uniqueName = uniqueName;
		this.levels = new ArrayList<DataQueryLevel>();
	}

	public void addMember(String level, String name, String uniqueName) {

		for (DataQueryLevel l : levels) {
			if (l.getUniqueName().equals(level)) {
				l.addMember(name, uniqueName);
				return;
			}
		}
		DataQueryLevel newLevel = new DataQueryLevel(level);
		newLevel.addMember(name, uniqueName);
		levels.add(newLevel);
	}
	
	public String getUniqueName() {
		return uniqueName;
	}
	
	public List<DataQueryLevel> getLevels() {
		return levels;
	}
}
