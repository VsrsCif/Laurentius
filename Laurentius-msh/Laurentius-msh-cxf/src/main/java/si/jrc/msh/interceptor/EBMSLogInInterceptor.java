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
package si.jrc.msh.interceptor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.DelegatingInputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import si.jrc.msh.utils.EBMSLogUtils;
import si.laurentius.commons.cxf.EBMSConstants;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSLogInInterceptor extends AbstractSoapInterceptor {

  private static final SEDLogger LOG = new SEDLogger(EBMSLogInInterceptor.class);

  /**
   *
   */
  public EBMSLogInInterceptor() {
    super(Phase.RECEIVE);
  }

  /**
   *
   * @param message
   * @return
   */
  public String getURI(Message message) {
    String uri = (String) message.get(Message.REQUEST_URL);
    if (uri == null) {
      String address = (String) message.get(Message.ENDPOINT_ADDRESS);
      uri = (String) message.get(Message.REQUEST_URI);
      if (uri != null && uri.startsWith("/")) {
        if (address != null && !address.startsWith(uri)) {
          if (address.endsWith("/") && address.length() > 1) {
            address = address.substring(0, address.length());
          }
          uri = address + uri;
        }
      } else {
        uri = address;
      }
    }
    return uri;
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
      throws Fault {
    long l = LOG.logStart();
   

    boolean isRequestor = MessageUtils.isRequestor(msg);
    String base = (String) msg.getExchange().get(EBMSConstants.EBMS_CP_BASE_LOG_SOAP_MESSAGE_FILE);
    File fLog = EBMSLogUtils.getInboundFileName(isRequestor, base);
    base = EBMSLogUtils.getBaseFileName(fLog);
    msg.getExchange().put(EBMSConstants.EBMS_CP_BASE_LOG_SOAP_MESSAGE_FILE, base);
    msg.getExchange().put(EBMSConstants.EBMS_CP_IN_LOG_SOAP_MESSAGE_FILE, fLog);

    LOG.log("In from: '" + getURI(msg) + "' " + (isRequestor ? "response" : "request") +
         " stored to:" + fLog.getName());

    //  create FileOutputStream to log request 
    logging(fLog, msg);

  }

  /**
   *
   * @param message
   * @param is
   */
  protected void logInputStream(Message message, InputStream is, File fLog) {
    long l = LOG.logStart();
    //  create FileOutputStream to log request 

    try {
      // use the appropriate input stream and restore it later
      InputStream bis =
          is instanceof DelegatingInputStream ? ((DelegatingInputStream) is).getInputStream() : is;

      try (FileOutputStream fos = new FileOutputStream(fLog)) {
        IOUtils.copy(bis, fos);
        fos.flush();
      } catch (IOException ex) {
        String errmsg =
            "Could not log inbound message to file: '" + fLog.getAbsolutePath() + "'! ";
        LOG.logError(l, errmsg, ex);
        return;
      }

      FileInputStream fis = new FileInputStream(fLog);


      // restore the delegating input stream or the input stream
      if (is instanceof DelegatingInputStream) {
        ((DelegatingInputStream) is).setInputStream(fis);
      } else {
        message.setContent(InputStream.class, fis);
      }

    
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  /**
   *
   * @param logger
   * @param message
   * @throws Fault
   */
  protected void logging( File fout, Message message)
      throws Fault {

    final LoggingMessage buffer =
        new LoggingMessage(fout, "IN MESSAGE");

    if (!Boolean.TRUE.equals(message.get(Message.DECOUPLED_CHANNEL_MESSAGE))) {
      // avoid logging the default responseCode 200 for the decoupled responses
      Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
      if (responseCode != null) {
        buffer.getResponseCode().append(responseCode);
      }
    }

    String encoding = (String) message.get(Message.ENCODING);

    if (encoding != null) {
      buffer.getEncoding().append(encoding);
    }
    String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
    if (httpMethod != null) {
      buffer.getHttpMethod().append(httpMethod);
    }
    String ct = (String) message.get(Message.CONTENT_TYPE);
    if (ct != null) {
      buffer.getContentType().append(ct);
    }
    Object headers = message.get(Message.PROTOCOL_HEADERS);

    if (headers != null) {
      buffer.getHeader().append(headers);
    }
    String uri = (String) message.get(Message.REQUEST_URL);
    if (uri == null) {
      String address = (String) message.get(Message.ENDPOINT_ADDRESS);
      uri = (String) message.get(Message.REQUEST_URI);
      if (uri != null && uri.startsWith("/")) {
        if (address != null && !address.startsWith(uri)) {
          if (address.endsWith("/") && address.length() > 1) {
            address = address.substring(0, address.length());
          }
          uri = address + uri;
        }
      } else {
        uri = address;
      }
    }
    if (uri != null) {
      buffer.getAddress().append(uri);
      String query = (String) message.get(Message.QUERY_STRING);
      if (query != null) {
        buffer.getAddress().append("?").append(query);
      }
    }

    LOG.log(buffer.toString());
    InputStream is = message.getContent(InputStream.class);
    
    logInputStream(message, is,fout);

  }
}
