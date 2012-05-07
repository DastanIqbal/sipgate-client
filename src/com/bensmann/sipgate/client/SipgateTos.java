package com.bensmann.sipgate.client;

public enum SipgateTos {

    VOICE,

    TEXT,

    FAX;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
