package com.bensmann.sipgate.client;

public class SipgateClientFactoryException extends RuntimeException {

    public SipgateClientFactoryException(Throwable cause) {
        super(cause);
    }

    public SipgateClientFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SipgateClientFactoryException(String message) {
        super(message);
    }

}
