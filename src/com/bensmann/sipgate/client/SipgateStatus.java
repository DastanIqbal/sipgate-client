package com.bensmann.sipgate.client;

public enum SipgateStatus {

    INCOMING,

    OUTGOING,

    ACCEPTED,

    MISSED;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
