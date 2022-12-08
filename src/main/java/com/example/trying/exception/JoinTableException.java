package com.example.trying.exception;

public class JoinTableException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JoinTableException(String errorMessage) {
		super(errorMessage);
	}
}
