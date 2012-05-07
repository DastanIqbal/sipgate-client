package com.bensmann.sipgate.client;

/**
 *
 * @author rbe
 */
public class SipgateClientException extends Exception {

    public SipgateClientException(Throwable cause) {
        super(cause);
    }

    public SipgateClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SipgateClientException(String message) {
        super(message);
    }

}
