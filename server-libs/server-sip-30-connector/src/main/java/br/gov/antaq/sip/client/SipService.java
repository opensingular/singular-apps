
package br.gov.antaq.sip.client;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "sipService", targetNamespace = "sipns", wsdlLocation = "file:/home/torquato/desenv/workspace/outorga/integracao/client/sip-client/src/main/resources/ws_sip_autenticar.wsdl")
public class SipService
    extends Service
{

    private final static URL SIPSERVICE_WSDL_LOCATION;
    private final static WebServiceException SIPSERVICE_EXCEPTION;
    private final static QName SIPSERVICE_QNAME = new QName("sipns", "sipService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/home/torquato/desenv/workspace/outorga/integracao/client/sip-client/src/main/resources/ws_sip_autenticar.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        SIPSERVICE_WSDL_LOCATION = url;
        SIPSERVICE_EXCEPTION = e;
    }

    public SipService() {
        super(__getWsdlLocation(), SIPSERVICE_QNAME);
    }

    public SipService(WebServiceFeature... features) {
        super(__getWsdlLocation(), SIPSERVICE_QNAME, features);
    }

    public SipService(URL wsdlLocation) {
        super(wsdlLocation, SIPSERVICE_QNAME);
    }

    public SipService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SIPSERVICE_QNAME, features);
    }

    public SipService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SipService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns SipPortType
     */
    @WebEndpoint(name = "sipPortType")
    public SipPortType getSipPortType() {
        return super.getPort(new QName("sipns", "sipPortType"), SipPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SipPortType
     */
    @WebEndpoint(name = "sipPortType")
    public SipPortType getSipPortType(WebServiceFeature... features) {
        return super.getPort(new QName("sipns", "sipPortType"), SipPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (SIPSERVICE_EXCEPTION!= null) {
            throw SIPSERVICE_EXCEPTION;
        }
        return SIPSERVICE_WSDL_LOCATION;
    }

}
