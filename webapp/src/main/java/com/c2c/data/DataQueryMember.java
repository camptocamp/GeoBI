package com.c2c.data;

public class DataQueryMember {
	
	private String name;
	private String uniqueName;
	
	public DataQueryMember(String name, String uniqueName) {
		this.name = name;
		this.uniqueName = uniqueName;
	}

	public String getName() {
		return name;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}
}
