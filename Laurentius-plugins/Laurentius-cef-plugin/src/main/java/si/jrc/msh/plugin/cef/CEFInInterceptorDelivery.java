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
package si.jrc.msh.plugin.cef;

import java.util.Properties;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.apache.cxf.binding.soap.SoapMessage;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.ebox.SEDBox;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SoapInterceptorInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class CEFInInterceptorDelivery implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(CEFInInterceptorDelivery.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg) {
    long l = LOG.logStart();

    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);
    
    // sender
    // receiver
    

    MSHOutMail mout = new MSHOutMail();
    mout.setMSHOutProperties(new MSHOutProperties());
    mout.setMessageId(Utils.getInstance().getGuidString());
    
    mout.setService(mInMail.getService());
    mout.setAction("Deliver");
    mout.setConversationId(mInMail.getConversationId());
    mout.setSenderEBox("laurentius-c3@mb-laurentius.si");
    mout.setSenderName("laurentius-c3");
    mout.setSenderEBox("minder@cef-minder.eu");
    mout.setReceiverName("minder");
    
    
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_SERVICE_PROP, mInMail.getService()));
    
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_ACTION_PROP, mInMail.getAction()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_CONV_ID, mInMail.getConversationId()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_FROM_PARTY_ID_PROP, mInMail.getSenderName()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_TO_PARTY_ROLE_PROP, mInMail.getReceiverName()));
    

    // add payload
    mout.setMSHOutPayload(new MSHOutPayload());

    for (MSHInPart mip : mInMail.getMSHInPayload().getMSHInParts()) {
      MSHOutPart mop = new MSHOutPart();
      mop.setType(mip.getType());
      mop.setDescription(mip.getDescription());
      mop.setEncoding(mip.getEncoding());
      mop.setFilename(mip.getFilename());
      mop.setFilepath(mip.getFilepath());
      mop.setMimeType(mip.getMimeType());
      mop.setName(mip.getName());
      mop.setMd5(mip.getMd5());

      mout.getMSHOutPayload().getMSHOutParts().add(mop);

    }

    try {
      mDB.serializeOutMail(mout, "", "CEFInInterceptor", "");
    } catch (StorageException ex) {
      LOG.logError(l, ex);
    }

    LOG.logEnd(l);
  }

  private MSHOutProperty createMSHOutProperty(String prpName, String val) {
    MSHOutProperty mop = new MSHOutProperty();
    mop.setName(prpName);
    mop.setName(val);
    return mop;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t) {
    // ignore
  }

}
