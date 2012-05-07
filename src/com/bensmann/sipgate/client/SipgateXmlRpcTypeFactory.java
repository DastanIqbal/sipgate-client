package com.bensmann.sipgate.client;

import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.serializer.StringSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author rbe
 */
public class SipgateXmlRpcTypeFactory extends TypeFactoryImpl {

    private static final TypeSerializer myStringSerializer = new StringSerializer() {

        @Override
        public void write(ContentHandler pHandler, Object pObject) throws SAXException {
            write(pHandler, STRING_TAG, pObject.toString());
        }

    };

    public SipgateXmlRpcTypeFactory(XmlRpcController pController) {
        super(pController);
    }

    @Override
    public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
        if (pObject instanceof String) {
            return myStringSerializer;
        }
        return super.getSerializer(pConfig, pObject);
    }

}
