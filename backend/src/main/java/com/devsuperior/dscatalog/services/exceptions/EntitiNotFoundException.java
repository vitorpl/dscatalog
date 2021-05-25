package com.devsuperior.dscatalog.services.exceptions;

import javax.persistence.EntityNotFoundException;

public class EntitiNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -399081935425992696L;

	public EntitiNotFoundException(String msg) {
		super(msg);
	}	
	
}
