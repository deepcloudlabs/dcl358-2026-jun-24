package com.example.om.service;

public class MessagePublicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public MessagePublicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
