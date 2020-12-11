package com.network.server.console;

public class InvalidSyntaxException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidSyntaxException() {
		super();
	}

	public InvalidSyntaxException(String message) {
		super(message);
	}
}
