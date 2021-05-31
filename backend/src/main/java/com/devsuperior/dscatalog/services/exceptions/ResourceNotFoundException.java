package com.devsuperior.dscatalog.services.exceptions;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -399081935425992696L;

	public ResourceNotFoundException(String msg) {
		super(msg);
	}	
	
}
