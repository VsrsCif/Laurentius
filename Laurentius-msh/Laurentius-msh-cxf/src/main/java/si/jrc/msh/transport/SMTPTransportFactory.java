/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.transport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class SMTPTransportFactory extends AbstractTransportFactory
 implements  ConduitInitiator {

 

  //public static final String TRANSPORT_ID = "http://cxf.apache.org/transports/smtp";
  public static final String TRANSPORT_ID = "http://schemas.xmlsoap.org/soap/http";
  public static final List<String> DEFAULT_NAMESPACES =
      Arrays.asList(TRANSPORT_ID);

  private static final Logger LOG = LogUtils.getL7dLogger(SMTPTransportFactory.class);
  private static final Set<String> URI_PREFIXES = new HashSet<String>();

  static {
    URI_PREFIXES.add("smtp://");
    URI_PREFIXES.add("smtp:");
  }
  
   static {
    Bus bus = BusFactory.getDefaultBus();
    
    SMTPTransportFactory customTransport = new SMTPTransportFactory();
    

    ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
    extension.registerConduitInitiator(TRANSPORT_ID, customTransport);
  
  }

  private final Set<String> uriPrefixes = new HashSet<>(URI_PREFIXES);

  public SMTPTransportFactory() {
    super(DEFAULT_NAMESPACES);
  }

  @Override
  public Conduit getConduit(EndpointInfo ei, Bus bus)
      throws IOException {
    return getConduit(ei, null, bus);
  }

  @Override
  public Conduit getConduit(EndpointInfo ei, EndpointReferenceType target, Bus bus)
      throws IOException {
    LOG.log(Level.FINE, "Creating conduit for {0}", ei.getAddress());
    if (target == null) {
      target = createReference(ei);
    }
    return new SMTPConduit(target, bus);
  }

 
  @Override
  public Set<String> getUriPrefixes() {
    return uriPrefixes;
  }

  EndpointReferenceType createReference(EndpointInfo ei) {
    EndpointReferenceType epr = new EndpointReferenceType();
    AttributedURIType address = new AttributedURIType();
    address.setValue(ei.getAddress());
    epr.setAddress(address);
    return epr;
  }

}
