package com.bensmann.sipgate.client;

import com.bensmann.eco.helper.prefs.PropertiesFactory;
import java.io.IOException;
import java.util.Properties;

/**
 */
public final class SipgateClientFactory {

    /**
     */
    private SipgateClientFactory() {
    }

    /**
     * @param username
     * @param password
     * @return
     */
    public static SipgateClient create(String username, String password) {
        SipgateClient client = null;
        try {
            // Load properties
            Properties p = PropertiesFactory.create(SipgateClientFactory.class, "sipgate", null);
            // Add username and password
            p.setProperty("username", username);
            p.setProperty("password", password);
            // Instantiate Sipgate client
            client = new SipgateClient(p);
            try {
                client.login();
            } catch (SipgateClientException e) {
                throw new SipgateClientFactoryException(e);
            }
        } catch (IOException e) {
            throw new SipgateClientFactoryException("Could not read properties", e);
        }
        return client;
    }

}
