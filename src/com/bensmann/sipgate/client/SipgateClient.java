package com.bensmann.sipgate.client;

import com.bensmann.eco.helper.io.IOHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.ws.commons.util.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

/**
 *
 * @author rbe
 */
public final class SipgateClient {

    /**
     */
    private static final Logger logger;

    /**
     */
    private Properties properties;

    /**
     */
    private XmlRpcClient xmlrpcClient;

    /**
     */
    private final Object[] EMPTY;

    /**
     */
    private SipgateUri[] localUriList;

    /**
     *
     */
    private SipgateHelper sgh;

    static {
        logger = Logger.getLogger(SipgateClient.class.getName());
    }

    /**
     * @param properties
     */
    public SipgateClient(Properties properties) {
        this.properties = properties;
        EMPTY = new Object[0];
        sgh = SipgateHelper.getInstance();
    }

    /**
     * Get list of local uris (voip numbers).
     * @return Array of SipgateUri instances.
     * @throws SipgateClientException
     */
    public synchronized SipgateUri[] getLocalUriList() throws SipgateClientException {
        if (null == localUriList) {
            Map<?, ?> result = exe("samurai.OwnUriListGet");
            Object[] ownUriList = (Object[]) result.get("OwnUriList");
            localUriList = new SipgateUri[ownUriList.length];
            SipgateUri uri = null;
            for (int i = 0; i < ownUriList.length; i++) {
                Map<?, ?> m = (Map<?, ?>) ownUriList[i];
                uri = new SipgateUri();
                uri.setUri((String) m.get("SipUri"));
                Object[] tos = (Object[]) m.get("TOS");
                String[] s = new String[tos.length];
                for (int j = 0; j < tos.length; j++) {
                    s[j] = (String) tos[j];
                }
                uri.setTos(s);
                uri.setNumber((String) m.get("E164Out"));
                localUriList[i] = uri;
            }
        }
        return localUriList;
    }

    /**
     * Execute a command.
     * @param cmd Command name.
     * @param value Map with arguments to command.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    private synchronized Map<?, ?> exe(String cmd, Map<String, Object> value) throws SipgateClientException {
        // Pre-condition: xmlrpcClient != null
        login();
        if (null == xmlrpcClient) {
            throw new IllegalStateException("No XML RPC client");
        }
        try {
            logger.fine("executing cmd=" + cmd);
            for (String k : value.keySet()) {
                logger.fine(" ... " + k + " = " + value.get(k));
            }
            Map<?, ?> map = (Map<?, ?>) xmlrpcClient.execute(cmd, new Object[]{value});
            for (Object k : map.keySet()) {
                logger.fine("" + k + " = " + map.get(k));
            }
            return map;
        } catch (XmlRpcException e) {
            value.put("StatusCode", e.code);
            logger.warning("XML-RPC FAULT CODE: " + e.code + " for cmd " + cmd);
            //ogger.log(Level.SEVERE, "Could not execute XML-RPC command", e);
            //throw new SipgateClientException(e);
            return value;
        }
    }

    /**
     * Execute a command.
     * @param cmd Command name.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    private synchronized Map<?, ?> exe(String cmd) throws SipgateClientException {
        // Pre-condition: xmlrpcClient != null
        login();
        if (null == xmlrpcClient) {
            throw new IllegalStateException("No XML RPC client");
        }
        try {
            return (Map<?, ?>) xmlrpcClient.execute(cmd, EMPTY);
        } catch (XmlRpcException e) {
            throw new SipgateClientException(e);
        }
    }

    /**
     * Initialize XML-RPC for sipgate and perform 'ClientIdentify'.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public synchronized Map<?, ?> login() throws SipgateClientException {
        Map<?, ?> result = null;
        if (null == xmlrpcClient) {
            try {
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                StringBuilder builder = new StringBuilder();
                builder.append("https://") /*.append(URLEncoder.encode(properties.getProperty("username"), "UTF-8"))
                        .append(":")
                        .append(properties.getProperty("password"))*/.append("api.sipgate.net/RPC2");
                logger.fine("connecting via URL=" + builder.toString());
                URL url = new URL(builder.toString());
                config.setServerURL(url);
                config.setBasicUserName(properties.getProperty("username"));
                config.setBasicPassword(properties.getProperty("password"));
                config.setEnabledForExceptions(true);
                xmlrpcClient = new XmlRpcClient();
                xmlrpcClient.setConfig(config);
                // XML-RPC TransportFactory
                XmlRpcCommonsTransportFactory factory = new XmlRpcCommonsTransportFactory(xmlrpcClient);
                xmlrpcClient.setTransportFactory(factory);
                // XML-RPC TypeFactory
                xmlrpcClient.setTypeFactory(new SipgateXmlRpcTypeFactory(xmlrpcClient));
                //
                result = exe("samurai.ClientIdentify", new HashMap<String, Object>() {

                    {
                        put("ClientVendor", properties.getProperty("client.vendor"));
                        put("ClientName", properties.getProperty("client.name"));
                        put("ClientVersion", properties.getProperty("client.version"));
                    }

                });
                // Get our SIP URIs
                localUriList = getLocalUriList();
            } catch (MalformedURLException e) {
                xmlrpcClient = null;
                throw new SipgateClientException(e);
            }
        }
        return result;
    }

    /**
     * Close a session.
     * @param sessionId Session ID from sipgate.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    private Map<?, ?> closeSession(final String sessionId) throws SipgateClientException {
        return exe("samurai.SessionClose", new HashMap<String, Object>() {

            {
                put("SessionID", sessionId);
            }

        });
    }

    /**
     * Close a session.
     * @param map Result map from sipgate XML-RPC service.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    private Map<?, ?> closeSession(final Map<?, ?> map) throws SipgateClientException {
        if (map != null && map.containsKey("SessionID")) {
            return closeSession((String) map.get("SessionID"));
        } else {
            logger.fine("No session ID");
            return null;
        }
    }

    /**
     * Get list of type of services.
     * @return String[] with TOSs.
     * @throws SipgateClientException
     */
    private String[] getTypeOfServiceList() throws SipgateClientException {
        String[] arr = null;
        Map tos = exe("samurai.TosListGet");
        Object[] o = (Object[]) tos.get("TosList");
        arr = new String[o.length];
        for (int i = 0; i < o.length; i++) {
            arr[i] = (String) o[i];
        }
        return arr;
    }

    /**
     * Send a short message.
     * @param localUri
     * @param remoteUri Remote uri.
     * @param text Message text.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public Map<?, ?> sendSms(final String localUri, final String[] remoteUri, final String text) throws SipgateClientException {
        // Check argument: number(s)
        if (null == remoteUri || (null != remoteUri && remoteUri.length == 0)) {
            throw new IllegalArgumentException();
        }
        // Check argument: text
        if (null == text || (null != text && text.length() == 0)) {
            throw new IllegalArgumentException();
        }
        // Send SMS
        final Map<?, ?> result = exe("samurai.SessionInitiateMulti", new Hashtable<String, Object>() {

            {
                put("LocalUri", "sip:4925159086000@sipgate.de"/*checkUri(localUri)*/);
                put("RemoteUri", sgh.getUriAsStringList(remoteUri));
                put("TOS", "text");
                put("Content", text);
                //put("Schedule", "");
            }

        });
        closeSession(result);
        return result;
    }

    /**
     * Send a PDF as fax.
     * @param localUri
     * @param remoteUri Remote uri.
     * @param pdf PDF file reference.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public Map<?, ?> sendFax(final String localUri, final String[] remoteUri, final File pdf) throws SipgateClientException {
        // Check argument: pdf
        if (null == pdf || (null != pdf && !pdf.exists() || !pdf.isFile() || !pdf.canRead())) {
            throw new IllegalArgumentException("No PDF given or not existant or not readable");
        }
        // Send fax
        try {
            return sendFax(localUri, remoteUri, IOHelper.getFileAsByteArray(pdf));
        } catch (IOException e) {
            throw new SipgateClientException("Could not encode PDF", e);
        }
    }

    /**
     * Send a PDF as fax.
     * @param localUri
     * @param remoteUri Remote uri.
     * @param pdf PDF as byte array.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public Map<?, ?> sendFax(final String localUri, final String[] remoteUri, final byte[] pdf) throws SipgateClientException {
        // Check argument: number(s)
        if (null == remoteUri || (null != remoteUri && remoteUri.length == 0)) {
            throw new IllegalArgumentException("No recipient number(s) given");
        }
        // Send fax
        Map result = null;
        // Encode
        final String b = Base64.encode(pdf);
        // Send
        result = exe("samurai.SessionInitiateMulti", new Hashtable<String, Object>() {

            {
                put("LocalUri", sgh.getUri(localUri, localUriList));
                put("RemoteUri", sgh.getUriAsStringList(remoteUri));
                put("TOS", "fax");
                put("Content", b);
            }

        });
        closeSession(result);
        return result;
    }

    /**
     * Initiate voice call.
     * @param localUri Local uri.
     * @param remoteUri Remote uri.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public Map<?, ?> initiateCall(final String localUri, final String remoteUri) throws SipgateClientException {
        // Check argument: number(s)
        if (null == remoteUri) {
            throw new IllegalArgumentException();
        }
        // Call other party
        Map result = exe("samurai.SessionInitiate", new Hashtable<String, Object>() {

            {
                put("LocalUri", sgh.getUri(localUri, localUriList));
                put("RemoteUri", new SipgateUri(remoteUri).getFormattedUri());
                put("TOS", "voice");
                put("Content", "");
            }

        });
        return result;
    }

    /**
     * Get summary of read and unread messages.
     * @param localUri Local uri; can be null.
     * @param tos TOS; can be null.
     * @param label Label; can be null.
     * @return Answer from sipgate.
     * @throws SipgateClientException
     */
    public Map<?, ?> getUnifiedMessagingSummary(final String[] localUri, final String[] tos, final String[] label) throws SipgateClientException {
        Map<?, ?> result = exe("samurai.UmSummaryGet", new Hashtable<String, Object>() {

            {
                if (null != localUri) {
                    put("LocalUriList", sgh.getUriAsStringList(localUri));
                }
                if (null != tos) {
                    put("TOS", new String[]{"voice", "text", "fax"});
                }
                if (null != label) {
                    put("Label", label);
                }
            }

        });
        return result;
    }

    /**
     * Get history entries.
     * @param localUri List of local uris; can be null.
     * @param status List of statuses: outgoing, accepted, missed; can be null.
     * @param start Start date; can be null.
     * @param end End date; can be null.
     * @return Array of SipgateHistoryEntry.
     * @throws SipgateClientException
     */
    public SipgateHistoryEntry[] getHistory(final String[] localUri, final String[] status, final Calendar start, final Calendar end) throws SipgateClientException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        final Vector<String> vec = new Vector<String>();
        if (null != status) {
            for (String s : status) {
                vec.add(SipgateStatus.valueOf(s).toString());
            }
        }
        Map result = exe("samurai.HistoryGetByDate", new Hashtable<String, Object>() {

            {
                if (null != localUri) {
                    put("LocalUriList", sgh.getUriAsStringList(localUri));
                }
                if (null != status) {
                    put("StatusList", vec);
                }
                if (null != start) {
                    put("PeriodStart", sdf.format(start.getTime()));
                }
                if (null != end) {
                    put("PeriodEnd", sdf.format(end.getTime()));
                }
            }

        });
        if (null != result) {
            Object[] entries = (Object[]) result.get("History");
            SipgateHistoryEntry[] she = new SipgateHistoryEntry[entries.length];
            Map m = null;
            for (int i = 0; i < entries.length; i++) {
                m = (Map) entries[i];
                SipgateHistoryEntry s = new SipgateHistoryEntry();
                s.setStatus(SipgateStatus.valueOf(((String) m.get("Status")).toUpperCase()));
                s.setTos(SipgateTos.valueOf(((String) m.get("TOS")).toUpperCase()));
                s.setLocalUri(new SipgateUri((String) m.get("LocalUri")));
                s.setRemoteUri(new SipgateUri((String) m.get("RemoteUri")));
                s.setEntryId((String) m.get("EntryID"));
                try {
                    s.setTimestamp(sdf.parse(((String) m.get("Timestamp"))));
                } catch (ParseException e) {
                    logger.log(Level.SEVERE, null, e);
                }
                she[i] = s;
            }
            return she;
        }
        return null;
    }

    /**
     * Get history entries.
     * @param localUri List of local uris; can be null.
     * @param status List of statuses: outgoing, accepted, missed; can be null.
     * @param start Start date
     * @return Array of SipgateHistoryEntry.
     * @throws SipgateClientException
     */
    public SipgateHistoryEntry[] getHistory(final String[] localUri, final String[] status, Calendar start) throws SipgateClientException {
        // Check argument
        if (null == start) {
            throw new IllegalArgumentException();
        }
        return getHistory(localUri, status, start, null);
    }

    /**
     * Get history entries for today.
     * @param localUri List of local uris; can be null.
     * @param status List of statuses: outgoing, accepted, missed; can be null.
     * @return Array of SipgateHistoryEntry.
     * @throws SipgateClientException
     */
    public SipgateHistoryEntry[] getTodaysHistory(final String[] localUri, final String[] status) throws SipgateClientException {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        return getHistory(localUri, status, start, end);
    }

    /**
     * Get history entries.
     * @return 
     * @throws SipgateClientException
     */
    public Map<?, ?> getEventList() throws SipgateClientException {
        Map<?, ?> result = exe("samurai.EventListGet", new Hashtable<String, Object>() {

            {
                put("Labels", new String[]{});
                //put("EventIDs", new String[]{});
                //put("TOS", new String[]{"voice", "fax", "text"});
                put("Limit", 0);
                put("Offset", 0);
                //put("PeriodStart", null);
                //put("PeriodEnd", null);
                //put("IncrementBaseID", "");
            }

        });
        return result;
    }

    /*
    public static void main(String[] args) throws Exception {
        try {
            LogManager.getLogManager().readConfiguration(SipgateClient.class.getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            //logger.log(Level.SEVERE, "Cannot read configuration for Java Logging API", e);
        }
        //System.setProperty("javax.net.ssl.trustStore", "C:/Program Files/Java/jdk1.6.0_13/jre/lib/security/cacerts");
        //System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SipgateClient s = SipgateClientFactory.create("sipgate@bensmann.com", "xxxx");
        Map<?, ?> m = s.exe("samurai.BalanceGet");
        for (Object k : m.keySet()) {
            logger.fine("" + k + " = " + m.get(k));
        }
        try {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.MONTH, 0);
            start.set(Calendar.YEAR, 2010);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            SipgateHistoryEntry[] he = s.getHistory(new String[]{"4925159086000"}, new String[]{"INCOMING"}, start);
            for (SipgateHistoryEntry e : he) {
                logger.fine("" + e.toString());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
        String[] smsUri = new String[]{"4915156108857"};
        Map<?, ?> map = null;
        try {
            //map = s.sendSms("4925159086000", smsUri, "eine test sms");
            //map = s.initiateCall("4925159086000", "4915156108857");
            map = s.getEventList();
            //Object[] arr = (Object[]) map.get("EventList");
            for (Object o : (Object[]) map.get("EventList")) {
                System.out.println("" + o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

}
