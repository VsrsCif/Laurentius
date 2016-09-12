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
public class CEFInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(CEFInInterceptor.class);

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

    Properties p = new Properties();
    mInMail.getMSHInProperties().getMSHInProperties().forEach(sp -> {
      p.put(sp.getName(), sp.getValue());
    });

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getInstance().getGuidString());

    setValue(CEFConstants.S_SERVICE_PROP, p, mout);
    setValue(CEFConstants.S_ACTION_PROP, p, mout);
    setValue(CEFConstants.S_CONV_ID, p, mout);
    setValue(CEFConstants.S_TO_PARTY_ID_PROP, p, mout);
    setValue(CEFConstants.S_FROM_PARTY_ID_PROP, p, mout);
    setValue(CEFConstants.S_TO_PARTY_ROLE_PROP, p, mout);
    setValue(CEFConstants.S_FROM_PARTY_ROLE_PROP, p, mout);

    mout.setMSHOutProperties(new MSHOutProperties());

    for (String key : p.stringPropertyNames()) {
      MSHOutProperty mop = new MSHOutProperty();
      mop.setName(key);
      mop.setValue(p.getProperty(key));
      mout.getMSHOutProperties().getMSHOutProperties().add(mop);

    }
    mout.setMSHOutPayload(new MSHOutPayload());
    
    for (MSHInPart mip: mInMail.getMSHInPayload().getMSHInParts()){
      MSHOutPart  mop = new MSHOutPart();
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

  private void setValue(String prpName, Properties p, MSHOutMail mo) {
    if (p.containsKey(prpName)) {
      String val = p.getProperty(prpName);
      p.remove(prpName);
      switch (prpName) {
        case CEFConstants.S_SERVICE_PROP:
          mo.setService(val);
          break;
        case CEFConstants.S_ACTION_PROP:
          mo.setAction(val);
          break;
        case CEFConstants.S_CONV_ID:
          mo.setConversationId(val);
          break;
        case CEFConstants.S_TO_PARTY_ID_PROP:
          mo.setReceiverName(val);
          mo.setReceiverEBox(val + "@mb-laurentius.si");
          break;
        case CEFConstants.S_FROM_PARTY_ID_PROP:
          mo.setSenderName(val);
          mo.setSenderEBox(val + "@" + SEDSystemProperties.getLocalDomain());
          break;
        case CEFConstants.S_FROM_PARTY_ROLE_PROP:
          break;
        case CEFConstants.S_TO_PARTY_ROLE_PROP:
          break;

      }

    } else {
      LOG.formatedWarning("In propertey %s not exists", prpName);
    }

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
