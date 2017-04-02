/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.msh.test.db;

import java.security.Principal;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.w3c.dom.Element;

/**
 *
 * @author sluzba
 */
public class TestWebServiceContext implements WebServiceContext{
  
  Principal p = null;
  
  public  TestWebServiceContext(String principal) {
    p = new Principal() {
      String prnp = principal;
      @Override
      public String getName() {
        return prnp;
      }
    };
  }
        

  @Override
  public EndpointReference getEndpointReference(Element... referenceParameters) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
          Element... referenceParameters) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public MessageContext getMessageContext() {
    return null;
  }

  @Override
  public Principal getUserPrincipal() {
    return p;
  }

  @Override
  public boolean isUserInRole(String role) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
