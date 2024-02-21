package com.dwsoft.webapp.user.dto;

import java.io.Serializable;

public class UserTagDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 735357392585311291L;

	private String key;
	private String type;
	private String label;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
