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
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class CEFInFaultInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(CEFInFaultInterceptor.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPMode;

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

 @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef def = new MailInterceptorDef();
    def.setType("CEFInFaultInterceptor");
    def.setName("CEFInFaultInterceptor");
    def.setDescription("CEF Digital testin module");
    return def;
  }
  

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
    long l = LOG.logStart();
    
    // !!!!!!!!!!!!!!!!!!!!!!!
    // Error notification is handled by OutMailEventListnere
    
    /*boolean isBackChannel = SoapUtils.isRequestMessage(msg);

    SEDBox inboxSb = SoapUtils.getMSHInMailReceiverBox(msg);

    MSHOutMail mOutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);
    if (mInMail == null && isBackChannel) {
      if (mOutMail != null) {
        String type = "Error";
        fireAdviceOfDeliveryNotification(mOutMail, inboxSb, type );
      }      
    }*/
   
    LOG.logEnd(l);
    return true;
  }

 

  /**
   * Method submits mail of succesful deliery
   *
   * @param mOutMail
   * @param sbInbox
   * @param type
   *
  public void fireAdviceOfDeliveryNotification(MSHOutMail mOutMail, SEDBox sbInbox, String type) {
    long l = LOG.logStart();

    // sender
    // receiver
    MSHOutMail mout = new MSHOutMail();
    mout.setMSHOutProperties(new MSHOutProperties());
    mout.setMessageId(Utils.getInstance().getGuidString());

    mout.setService(CEFConstants.S_SERVICE_CONF_TEST_ID);
    mout.setAction(CEFConstants.S_ACTION_CONF_TEST_NOTIFY);
    mout.setConversationId(mOutMail.getConversationId());
    mout.setRefToMessageId(mOutMail.getMessageId());
    mout.setSenderName(mOutMail.getSenderName());
    mout.setSenderEBox(mOutMail.getSenderEBox());
    mout.setReceiverName(CEFConstants.S_MINDER_NAME);
    mout.setReceiverEBox(CEFConstants.S_MINDER_ADDRESS);

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_SERVICE_PROP, mOutMail.getService()));

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_ACTION_PROP, mOutMail.getAction()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_CONV_ID, mOutMail.getConversationId()));
    
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_FROM_PARTY_ID_PROP, mOutMail.getSenderEBox().substring(0,mOutMail.getSenderEBox().indexOf("@"))));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_TO_PARTY_ID_PROP, mOutMail.getReceiverEBox().substring(0,mOutMail.getReceiverEBox().indexOf("@")  )));
  
        mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_REF_MESSAGE_ID, mOutMail.getMessageId() ));
         mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_SIGNAL_TYPE_ID, type));


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
    mop.setValue(val);
    return mop;
  }

*/
  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t, Properties contextProperties) {
    // ignore
  }

}
