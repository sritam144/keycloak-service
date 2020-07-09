package com.asiczen.identity.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AccessisDeniedException extends RuntimeException {

	private static final long serialVersionUID = 3375390057580825611L;

	public AccessisDeniedException() {
		super();
	}

	public AccessisDeniedException(String message) {
		super(message);
	}

	public AccessisDeniedException(String message, Throwable cause) {
		super(message, cause);
	}
}
