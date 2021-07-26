package com.devsuperior.dscatalog.dto;

public class UserInsertDTO extends UserDTO {

	private static final long serialVersionUID = 5099868662655233602L;
	private String password;
	
	private UserInsertDTO() {
		super();
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	

}
