package com.bensmann.sipgate.client;

import java.util.Arrays;
import java.util.logging.Logger;

public class SipgateUri {

    private static final Logger logger;

    private String uri;

    private String[] tos;

    private String number;

    static {
        logger = Logger.getLogger(SipgateUri.class.getName());
    }

    public SipgateUri() {
    }

    public SipgateUri(String uri) {
        logger.fine("uri="+uri);
        this.uri = uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setTos(String[] tos) {
        this.tos = tos;
    }

    public String[] getTos() {
        return tos;
    }

    public void setNumber(String outgoingNumber) {
        this.number = outgoingNumber;
    }

    public String getNumber() {
        return number;
    }

    public String getFormattedUri() {
        return uri.startsWith("sip") ? uri : "sip:" + uri + "@sipgate.net";
    }

    @Override
    public String toString() {
        /*
        StringBuilder builder = null;
        if (null != tos) {
            builder = new StringBuilder();
            for (int i = 0; i < tos.length; i++) {
                builder.append(tos[i]);
                if (i < tos.length - 1) {
                    builder.append(",");
                }
            }
        }
        return uri + (null != builder ? ("/" + builder.toString()) : "") + (null != number ? ("/" + number) : "");
        */
        return getFormattedUri();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SipgateUri)) {
            return false;
        }
        final SipgateUri other = (SipgateUri) object;
        if (!(uri == null ? other.uri == null : uri.equals(other.uri))) {
            return false;
        }
        if (!Arrays.equals(tos, other.tos)) {
            return false;
        }
        if (!(number == null ? other.number == null : number.equals(other.number))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 37;
        int result = 1;
        result = PRIME * result + ((uri == null) ? 0 : uri.hashCode());
        result = PRIME * result + Arrays.hashCode(tos);
        result = PRIME * result + ((number == null) ? 0 : number.hashCode());
        return result;
    }

}
