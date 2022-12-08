package com.example.trying.exception;

public class UncompatibleAnnotationException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UncompatibleAnnotationException(String errorMessage) {
		super(errorMessage);
	}
}
