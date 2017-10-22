/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author sluzba
 */
public class HTTPHandlerBasicAuthenticator implements SOAPHandler<SOAPMessageContext> {
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_BASIC = "Basic ";
  String token;

  public HTTPHandlerBasicAuthenticator(String username, String password) {
    this.token = HEADER_BASIC + " " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
    
  }
  
  

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    boolean request = ((Boolean) context.get(
            SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY));
    if (request) {
      Map<String, List<String>> headers = (Map<String, List<String>>) context
              .get(MessageContext.HTTP_REQUEST_HEADERS);
      if (null == headers) {
        headers = new HashMap<>();
      }
      headers.put(HEADER_AUTHORIZATION, Collections.singletonList(token));
      context.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }
    return true;
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {
  }

  @Override
  public Set<QName> getHeaders() {
    return null;
  }
}
