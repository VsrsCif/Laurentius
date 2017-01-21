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

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.msh.outbox.mail.MSHOutMail;

import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.plugin.eventlistener.OutMailEventListenerDef;
import si.laurentius.plugin.interceptor.MailInterceptorDef;

import si.laurentius.plugin.interfaces.OutMailEventInterface;
import si.laurentius.plugin.interfaces.OutMailEventInterface.PluginOutEvent;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(OutMailEventInterface.class)
public class CEFOutMailEventListener implements OutMailEventInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(CEFOutFaultInterceptor.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @Override
  public OutMailEventListenerDef getDefinition() {
    OutMailEventListenerDef def = new OutMailEventListenerDef();
    def.setType("CEFOutMailEventListener");
    def.setName("CEFOutMailEventListener");
    def.setDescription("CEF Digital testin module");
    return def;
  }
  

  @Override
  public void outEvent(MSHOutMail mi, EBMSMessageContext ctx, PluginOutEvent evnt) {

    if (evnt.equals(PluginOutEvent.FAILED)) {
      fireAdviceOfDeliveryNotification(mi, "Error");
    } else if (evnt.equals(PluginOutEvent.SEND)) {
      fireAdviceOfDeliveryNotification(mi, "Receipt");
    }

  }

  /**
   * Method submits mail of succesful deliery
   *
   * @param mOutMail
   *
   * @param type
   */
  public void fireAdviceOfDeliveryNotification(MSHOutMail mOutMail, String type) {
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
        CEFConstants.S_FROM_PARTY_ID_PROP, mOutMail.getSenderEBox().substring(0,
            mOutMail.getSenderEBox().indexOf("@"))));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_TO_PARTY_ID_PROP, mOutMail.getReceiverEBox().substring(0,
            mOutMail.getReceiverEBox().indexOf("@"))));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_REF_MESSAGE_ID, mOutMail.getMessageId()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
        CEFConstants.S_SIGNAL_TYPE_ID, type));

    try {
      mDB.serializeOutMail(mout, "", "fireAdviceOfDeliveryNotification", "");
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

}
