package com.example.trying.Constant;

public enum ReferenceOptions {
	NO_ACT("NO ACTION"),
	NULL("SET NULL"),
	RESTRICT("RESTRICT"),
	CASCADE("CASCADE");
	
	
	private String type;
	
	private ReferenceOptions(String type) {
		this.type= type;
	}
	
	
	public String getType() {
		return this.type;
	}
	
	

}
