package com.bensmann.sipgate.client;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rbe
 */
public final class SipgateHelper {

    private static final class H {

        private static final SipgateHelper INSTANCE = new SipgateHelper();

    }

    public static SipgateHelper getInstance() {
        return H.INSTANCE;
    }

    /**
     * Turn phone numbers into a list of URI strings.
     * @param uri String[] with phone numbers.
     * @return Array of SipgateUri instances.
     */
    public String[] getUriAsStringList(final String[] uri) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < uri.length; i++) {
            list.add(new SipgateUri(uri[i]).getFormattedUri());
        }
        return list.toArray(new String[0]);
    }

    /**
     * Turn a SIP uri into a local phone number.
     * @param number A phone number.
     * @return Array of possible uris.
     * @throws SipgateClientException
     */
    public String getNumberForSip(String sip, SipgateUri[] localSipUri) throws SipgateClientException {
        String num = null;
        for (SipgateUri localUri : localSipUri) {
            if (localUri.getFormattedUri().indexOf(sip) > -1) {
                num = localUri.getNumber();
                break;
            }
        }
        return num;
    }

    /**
     * Turn a local phone number into an uri.
     * @param number A phone number.
     * @return Array of possible uris.
     * @throws SipgateClientException
     */
    public String getSipForNumber(String number, SipgateUri[] localSipUri) throws SipgateClientException {
        String uri = null;
        for (SipgateUri localUri : localSipUri) {
            if (localUri.getNumber().equals(number)) {
                uri = localUri.getUri();
                break;
            }
        }
        return uri;
    }

    /**
     * Lookup SIP uri for a local voip number and/or check uri.
     * @param numberOrUri A local phone number or uri.
     * @return Uri.
     * @throws SipgateClientException
     */
    public String getUri(final String numberOrUri, SipgateUri[] localSipUri) throws SipgateClientException {
        String tmp = null;
        // Is it a phone number (then all characters will be digits)
        // or e.g. a sipgate account, starting with sip:?
        boolean sip = true;
        if (numberOrUri.startsWith("sip:")) {
            sip = false;
        }
        if (sip) {
            // Lookup
            tmp = getSipForNumber(numberOrUri, localSipUri);
            if (null == tmp) {
                throw new SipgateClientException("No local uri found for " + numberOrUri);
            }
            return tmp;
        }
        return numberOrUri;
    }

    /**
     * Lookup local voip number for SIP uri.
     * @param numberOrUri A local phone number or uri.
     * @return Local number.
     * @throws SipgateClientException
     */
    public String getNumber(final String numberOrUri, SipgateUri[] localSipUri) throws SipgateClientException {
        String tmp = null;
        // Is it a phone number (then all characters will be digits)
        // or e.g. a sipgate account, starting with sip:?
        boolean sip = false;
        if (numberOrUri.startsWith("sip:")) {
            sip = true;
        }
        if (sip) {
            // Lookup
            tmp = getNumberForSip(numberOrUri, localSipUri);
            if (null == tmp) {
                throw new SipgateClientException("No local uri found for " + numberOrUri);
            }
            return tmp;
        }
        return numberOrUri;
    }

}
