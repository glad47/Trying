package com.example.trying.exception;

public class ForignerKeyException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ForignerKeyException(String errorMessage) {
		super(errorMessage);
	}
}
