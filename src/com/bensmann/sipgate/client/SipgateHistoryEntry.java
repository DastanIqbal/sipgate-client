package com.bensmann.sipgate.client;

import java.util.Calendar;
import java.util.Date;

public class SipgateHistoryEntry {

    private SipgateStatus status;

    private SipgateTos tos;

    private SipgateUri localUri;

    private SipgateUri remoteUri;

    private String entryId;

    private Calendar timestamp;

    public SipgateHistoryEntry() {
    }

    public void setStatus(SipgateStatus status) {
        this.status = status;
    }

    public SipgateStatus getStatus() {
        return status;
    }

    public void setTos(SipgateTos tos) {
        this.tos = tos;
    }

    public SipgateTos getTos() {
        return tos;
    }

    public void setLocalUri(SipgateUri localUri) {
        this.localUri = localUri;
    }

    public SipgateUri getLocalUri() {
        return localUri;
    }

    public void setRemoteUri(SipgateUri remoteUri) {
        this.remoteUri = remoteUri;
    }

    public SipgateUri getRemoteUri() {
        return remoteUri;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        this.timestamp = cal;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SipgateHistoryEntry)) {
            return false;
        }
        final SipgateHistoryEntry other = (SipgateHistoryEntry) object;
        if (!(status == null ? other.status == null : status.equals(other.status))) {
            return false;
        }
        if (!(tos == null ? other.tos == null : tos.equals(other.tos))) {
            return false;
        }
        if (!(localUri == null ? other.localUri == null : localUri.equals(other.localUri))) {
            return false;
        }
        if (!(remoteUri == null ? other.remoteUri == null : remoteUri.equals(other.remoteUri))) {
            return false;
        }
        if (!(entryId == null ? other.entryId == null : entryId.equals(other.entryId))) {
            return false;
        }
        if (!(timestamp == null ? other.timestamp == null : timestamp.equals(other.timestamp))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 37;
        int result = 1;
        result = PRIME * result + ((status == null) ? 0 : status.hashCode());
        result = PRIME * result + ((tos == null) ? 0 : tos.hashCode());
        result = PRIME * result + ((localUri == null) ? 0 : localUri.hashCode());
        result = PRIME * result + ((remoteUri == null) ? 0 : remoteUri.hashCode());
        result = PRIME * result + ((entryId == null) ? 0 : entryId.hashCode());
        result = PRIME * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + tos + ":" + status + "/localuri=" + localUri + "/remoteuri=" + remoteUri +
            "/" + timestamp.getTime() + "]";
    }

}
