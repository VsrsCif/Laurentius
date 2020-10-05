/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.jrc.msh.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import si.jrc.msh.interceptor.*;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.Protocol;

import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.jrc.msh.transport.SMTPConduit;
import si.jrc.msh.transport.SMTPTransportFactory;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDCertUtilsInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;

/**
 * Sets up MSH client and submits message.
 *
 * @author Jože Rihtaršič
 */
public class MshClient {

  /**
   * Logger for MshClient class
   */
  protected final static SEDLogger LOG = new SEDLogger(MshClient.class);

  /**
   * Common Lookups from database
   */
//  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  //private SEDLookupsInterface mSedLookups;
  /**
   * Keystore tools
   */

  StorageUtils msStorageUtils = new StorageUtils();

  /**
   * Method sets set up a client for PartyIdentitySet.TransportProtocol.
   *
   * @param messageId
   * @param protocol: transport definition object frompmode
   * @param sec
   * @return Dispatch client for submitting message
   * @throws si.laurentius.commons.ebms.EBMSError (Error creating client)
   */
  public Dispatch<SOAPMessage> createClient(final String messageId,
          final PartyIdentitySet.TransportProtocol protocol,
          SEDCertUtilsInterface sec)
          throws EBMSError {

    if (protocol == null) {
      throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
              "Missing protocol!", SoapFault.FAULT_CODE_CLIENT);
    }

    if (protocol.getAddress() == null) {
      throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
              "Missing Address element!", SoapFault.FAULT_CODE_CLIENT);
    }
    if (protocol.getAddress().getValue() == null
            || protocol.getAddress().getValue().trim().isEmpty()) {
      throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
              "Empty address!",
              SoapFault.FAULT_CODE_CLIENT);
    }

    // --------------------------------------------------------------------
    // create MTOM service
    String url = protocol.getAddress().getValue();
    URL u = null;
    try {
      u = new URL(url);
    } catch (MalformedURLException ex) {
      throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
              "Invalid address" + url, ex,
              SoapFault.FAULT_CODE_CLIENT);

    }

    QName serviceName1 = new QName("", "");
    QName portName1 = new QName("", "");
    Service s = Service.create(serviceName1);
    s.addPort(portName1, SOAPBinding.SOAP12HTTP_MTOM_BINDING, url);
    MTOMFeature mtomFt = new MTOMFeature(true);

    Dispatch<SOAPMessage> dispSOAPMsg
            = s.createDispatch(portName1, SOAPMessage.class,
                    Service.Mode.MESSAGE, mtomFt);
    DispatchImpl dimpl = (org.apache.cxf.jaxws.DispatchImpl) dispSOAPMsg;
    SOAPBinding sb = (SOAPBinding) dispSOAPMsg.getBinding();
    sb.setMTOMEnabled(true);

    // --------------------------------------------------------------------
    // configure interceptors (log, ebms and plugin interceptors)
    Client cxfClient = dimpl.getClient();
    cxfClient.getInInterceptors().add(new EBMSLogInInterceptor());
    cxfClient.getInInterceptors().add(new EBMSInInterceptor());
    cxfClient.getInInterceptors().add(new MSHPluginInInterceptor());

    cxfClient.getOutInterceptors().add(new MSHPluginOutInterceptor());
    cxfClient.getOutInterceptors().add(new EBMSOutInterceptor());
    cxfClient.getOutInterceptors().add(new EBMSLogOutInterceptor());
    cxfClient.getInFaultInterceptors().add(new EBMSLogInInterceptor());
    cxfClient.getInFaultInterceptors().add(new EBMSInFaultInterceptor());
    cxfClient.getInFaultInterceptors().add(new MSHPluginInFaultInterceptor());
    cxfClient.getOutFaultInterceptors().add(new EBMSLogOutInterceptor());
    cxfClient.getOutFaultInterceptors().add(new EBMSOutFaultInterceptor());
    cxfClient.getOutFaultInterceptors().add(new MSHPluginOutFaultInterceptor());

    String url_protocol = u.getProtocol().toLowerCase();
    switch (url_protocol) {
      case "smtp": {// test smtp
        Bus bus = dimpl.getClient().getBus();
        ConduitInitiatorManager extension = bus.getExtension(
                ConduitInitiatorManager.class);
        extension.registerConduitInitiator(
                "http://schemas.xmlsoap.org/soap/http",
                new SMTPTransportFactory());
        SMTPConduit smtp = (SMTPConduit) cxfClient.getConduit();
        smtp.setSetJNDISession("java:jboss/mail/Default");
      }
      break;
      case "https": {
        // --------------------------------------------------------------------
        // set TLS
        if (protocol.getTLS() != null
                && (!Utils.isEmptyString(protocol.getTLS().
                        getServerTrustCertAlias()))
                || !Utils.isEmptyString(protocol.getTLS().getClientKeyAlias())) {
          setupTLS(messageId, (HTTPConduit) cxfClient.getConduit(), protocol.
                  getTLS(), sec);
        }
      } // do not break continue with http settings
      case "http": {
        HTTPConduit http = (HTTPConduit) cxfClient.getConduit();
        // --------------------------------------------------------------------
        // set http client policy
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        String host = System.getProperty(
                url_protocol.equals("http") ? SEDSystemProperties.PROXY_HTTP_HOST
                : SEDSystemProperties.PROXY_HTTPS_HOST);
        String port = System.getProperty(
                url_protocol.equals("http") ? SEDSystemProperties.PROXY_HTTP_PORT
                : SEDSystemProperties.PROXY_HTTPS_PORT);
        String noProxy = System.getProperty(
                url_protocol.equals("http") ? SEDSystemProperties.PROXY_HTTP_NO_PROXY
                : SEDSystemProperties.PROXY_HTTPS_NO_PROXY);

        if (!Utils.isEmptyString(host)) {
          int iport = 80;
          try {
            iport = Integer.parseInt(port);
          } catch (NumberFormatException ex) {
            LOG.formatedError(
                    "Invalid proxy port number %s for proxy host %s. Check proxy configuration (port 80 is setted)!",
                    port, host);

          }
          LOG.formatedDebug(
                  "Proxy: '%s:%d' (no proxy: '%s') is setted for url: %s", host,
                  iport, noProxy, url);
          httpClientPolicy.setProxyServer(host);
          httpClientPolicy.setProxyServerPort(iport);
          httpClientPolicy.setNonProxyHosts(noProxy);
        } else {
          LOG.formatedDebug("No proxy is setted for url: %s", url);
        }

        httpClientPolicy.setConnectionTimeout(protocol.getAddress().
                getConnectionTimeout() != null
                        ? protocol.getAddress().getConnectionTimeout() : 120000);
        httpClientPolicy.setReceiveTimeout(protocol.getAddress().
                getReceiveTimeout() != null
                        ? protocol.getAddress().getReceiveTimeout() : 120000);
        httpClientPolicy.setAllowChunking(
                protocol.getAddress().getChunked() != null
                ? protocol.getAddress().getChunked() : false);

        // set http Policy
        http.setClient(httpClientPolicy);
        break;
      }
      default: {
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
                "Invalid protocol: " + url_protocol + " for address: " + url,
                SoapFault.FAULT_CODE_CLIENT);
      }
    }

    return dispSOAPMsg;
  }

  /**
   * Method submits message according pmode configuration
   *
   * @param mail
   * @param ebms
   * @param sec
   * @return
   */
  public Result pushMessage(MSHOutMail mail, EBMSMessageContext ebms,
          SEDCertUtilsInterface sec) {

    long l = LOG.logStart(mail);
    Result r = new Result();

    Dispatch<SOAPMessage> client = null;
    try {

      client = createClient(mail.getMessageId(), ebms.getTransportProtocol(),
              sec);
      LOG.formatedlog("Create client for message  %s at %d", mail.getMessageId(), (LOG.getTime()-l));
       

      // set context parameters!
      SoapUtils.setEBMSMessageOutContext(ebms, client);
      SoapUtils.setMSHOutnMail(mail, client);
      mail.setSentDate(Calendar.getInstance().getTime());

      // create empty soap mesage
      MessageFactory mf = MessageFactory.newInstance(
              SOAPConstants.SOAP_1_2_PROTOCOL);
      SOAPMessage soapReq = mf.createMessage();
      LOG.formatedlog("Before send message  %s at %d", mail.getMessageId(), (LOG.getTime()-l));

      long st = LOG.getTime();
      LOG.formatedlog("Start submiting mail %s", mail.getMessageId());
      SOAPMessage soapRes = client.invoke(soapReq);
      LOG.formatedlog("Sent message  %s at %d", mail.getMessageId(), (LOG.getTime()-l));
      r.setResult(soapRes);
      LOG.formatedlog("Submit mail %s in ( %d ms).", mail.getMessageId(), (LOG.
              getTime() - st));
      
      
      if (client.getResponseContext().containsKey(EBMSConstants.EBMS_CP_OUTMAIL_RECIEPT)) {
        SignalMessage sm = (SignalMessage)client.getResponseContext().get(EBMSConstants.EBMS_CP_OUTMAIL_RECIEPT);
        mail.setReceivedDate(sm.getMessageInfo().getTimestamp());
      } else {
        mail.setReceivedDate(Calendar.getInstance().getTime());
      }
      LOG.formatedlog("Parse signalmessage  %s at %d", mail.getMessageId(), (LOG.getTime()-l));

      if (soapRes != null) {
        File file;
        try {
          file = StorageUtils.getNewStorageFile(MimeValue.MIME_XML.getSuffix(),
                  EBMSConstants.SOAP_PART_RESPONSE_PREFIX);
          try (FileOutputStream fos = new FileOutputStream(file)) {
            soapRes.writeTo(fos);
            String respFilePath = StorageUtils.getRelativePath(file);
            r.setResultFile(respFilePath);
            r.setMimeType(MimeValue.MIME_XML.getMimeType());

          } catch (IOException ex) {
            LOG.logError(l, "ERROR saving response to file!", ex);
          }
        } catch (StorageException ex) {
          LOG.logError(l, "ERROR saving response to file!", ex);
        }
      }
      LOG.formatedlog("Signalmessage stored  %s at %d", mail.getMessageId(), (LOG.getTime()-l));

    } catch (javax.xml.ws.WebServiceException ex) {

      String key = "org.apache.cxf.staxutils.W3CDOMStreamWriter";
      Throwable initCause = Utils.getInitCause(ex);

      if (client != null && client.getResponseContext().containsKey(key)) {

        EBMSError err = null;

        if ( client.getResponseContext().containsKey(
                EBMSConstants.EBMS_SIGNAL_ERRORS)) {
          List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error> lst
                  = (List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error>) client.
                          getResponseContext().get(
                                  EBMSConstants.EBMS_SIGNAL_ERRORS);
          if (!lst.isEmpty()) {
            EBMSErrorCode ec = EBMSErrorCode.
                    getByCode(lst.get(0).getErrorCode());
            err = new EBMSError(ec, mail.
                    getMessageId(), ex.getMessage(), ex,
                    SoapFault.FAULT_CODE_CLIENT);
          }
        }

        if (err == null) {
          err = new EBMSError(EBMSErrorCode.ApplicationError, mail.
                  getMessageId(),
                  "Soap fault error: " + ex.getMessage(), ex,
                  SoapFault.FAULT_CODE_CLIENT);

        }
        r.setError(err);

        W3CDOMStreamWriter wr = (W3CDOMStreamWriter) client.getResponseContext().
                get(key);

        try {
          File file = StorageUtils.getNewStorageFile(MimeValue.MIME_XML.
                  getSuffix(), EBMSConstants.SOAP_PART_FAULT_PREFIX);
          try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(wr.toString());
            //  wr.getDocument().
            r.setResultFile(StorageUtils.getRelativePath(file));
            r.setMimeType(MimeValue.MIME_XML.getMimeType());
          } catch (IOException ex1) {
            LOG.logError(l, "ERROR saving saop fault to file!", ex);
          }

        } catch (StorageException ex1) {
          LOG.logError(l, "ERROR saving saop fault to file!", ex);
        }
      } else if (initCause instanceof EBMSError) {
     
        r.setError((EBMSError) initCause);
      } else {
     
        r.setError(new EBMSError(EBMSErrorCode.DeliveryFailure, mail.
                getMessageId(),
                "HTTP error: " + Utils.getInitCauseMessage(ex), ex,
                SoapFault.FAULT_CODE_CLIENT));
        try {
          String res = msStorageUtils.storeThrowableAndGetRelativePath(ex);
          r.setMimeType(MimeValue.MIME_TXT.getMimeType());
          r.setResultFile(res);
        } catch (StorageException ex1) {
          LOG.logError(l, "ERROR saving saop fault to file!", ex1);
        }
      }
    } catch (SOAPException ex) {
      EBMSError err = null;

      if (client != null && client.getResponseContext().containsKey(
              EBMSConstants.EBMS_SIGNAL_ERRORS)) {
        List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error> lst
                = (List<org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error>) client.
                        getResponseContext().get(
                                EBMSConstants.EBMS_SIGNAL_ERRORS);
        if (!lst.isEmpty()) {
          EBMSErrorCode ec = EBMSErrorCode.getByCode(lst.get(0).getErrorCode());
          err = new EBMSError(ec, mail.
                  getMessageId(), ex.getMessage(), ex,
                  SoapFault.FAULT_CODE_CLIENT);
        }
      }

      if (err == null) {
        err = new EBMSError(EBMSErrorCode.ApplicationError, mail.
                getMessageId(),
                "Error occured while creating soap message!", ex,
                SoapFault.FAULT_CODE_CLIENT);

      }

      try {
        String res = msStorageUtils.storeThrowableAndGetRelativePath(ex);
        r.setMimeType(MimeValue.MIME_TXT.getMimeType());
        r.setResultFile(res);
      } catch (StorageException ex1) {
        LOG.logError(l, "ERROR saving saop fault to file!", ex);
      }
      r.setError(err);
    } catch (EBMSError ex) {
      try {
        String res = msStorageUtils.storeThrowableAndGetRelativePath(ex);
        r.setResultFile(res);
        r.setMimeType(MimeValue.MIME_TXT.getMimeType());
      } catch (StorageException ex1) {
        LOG.logError(l, "ERROR saving saop fault to file!", ex);
      }
      r.setError(ex);
    } catch (Throwable ex) {
      try {
        String res = msStorageUtils.storeThrowableAndGetRelativePath(ex);
        r.setResultFile(res);
        r.setMimeType(MimeValue.MIME_TXT.getMimeType());
      } catch (StorageException ex1) {
        LOG.logError(l, "Unexpected error!", ex);
      }
      r.setError(new EBMSError(EBMSErrorCode.ApplicationError, mail.
              getMessageId(),
              "Unexpected error!", ex, SoapFault.FAULT_CODE_CLIENT));
    }

    
    LOG.logEnd(l,  mail.getMessageId());
    return r;
  }

  /**
   * Method sets Truststore and key (if needed) to https client for TLS
   *
   * @param client - http(s) client
   * @param tls - pmode tls configuration
   * @throws FileNotFoundException
   * @throws IOException
   * @throws SEDSecurityException
   */
  private void setupTLS(String messageId, HTTPConduit httpConduit,
          Protocol.TLS tls, SEDCertUtilsInterface sec) {
    long l = LOG.logStart();
    TLSClientParameters tlsCP = null;
    // create 
    String serverTrustAlias = tls.getServerTrustCertAlias();
    String keyAlias = tls.getClientKeyAlias();
    LOG.formatedWarning("SET TLS : keyalias %s , cert alias %s", keyAlias,
            serverTrustAlias);

    // set trustore cert
    if (!Utils.isEmptyString(serverTrustAlias)) {
      tlsCP = new TLSClientParameters();
      serverTrustAlias = serverTrustAlias.trim();
      try {
        TrustManager[] trustStoreManagers = new TrustManager[]{sec.
          getTrustManagerForAlias(serverTrustAlias, true)};

        tlsCP.setTrustManagers(trustStoreManagers);
        tlsCP.setDisableCNCheck(tls.getDisableCNAndHostnameCheck() != null
                && tls.getDisableCNAndHostnameCheck());

      } catch (SEDSecurityException ex) {
        String msg = String.format(
                "(Message: %s) Error occured while creating TrustManagers for "
                + "truststore '%s'! Error: ", messageId, serverTrustAlias, ex.
                        getMessage());
        throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
                msg, ex, SoapFault.FAULT_CODE_CLIENT);
      }

      // set client's key cert for mutual identification
      if (!Utils.isEmptyString(keyAlias)) {
        keyAlias = keyAlias.trim();

        //get key managers
        KeyManager[] myKeyManagers;
        try {
          myKeyManagers = sec.getKeyManagerForAlias(keyAlias);
        } catch (SEDSecurityException ex) {
          String msg = String.format(
                  "(Message: %s) Error occured while creating KeyManagers for "
                  + "truststore '%s'! Error: ", messageId, serverTrustAlias, ex.
                          getMessage());
          throw new EBMSError(EBMSErrorCode.PModeConfigurationError, messageId,
                  msg, ex, SoapFault.FAULT_CODE_CLIENT);
        }

        tlsCP.setKeyManagers(myKeyManagers);
      }
      httpConduit.setTlsClientParameters(tlsCP);
    }
    LOG.logEnd(l,messageId);
  }

}
