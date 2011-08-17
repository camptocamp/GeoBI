package com.c2c.data;

import java.util.ArrayList;
import java.util.List;

public class DataQueryLevel {
	
	private String uniqueName;
	private List<DataQueryMember> members;
	
	public DataQueryLevel(String uniqueName) {
		this.uniqueName = uniqueName;
		this.members = new ArrayList<DataQueryMember>();
	}

	public void addMember(String name, String uniqueName) {

		for (DataQueryMember m : members) {
			if (m.getUniqueName().equals(uniqueName)) {
				return;
			}
		}
		members.add(new DataQueryMember(name, uniqueName));
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public List<DataQueryMember> getMembers() {
		return members;
	}
}
