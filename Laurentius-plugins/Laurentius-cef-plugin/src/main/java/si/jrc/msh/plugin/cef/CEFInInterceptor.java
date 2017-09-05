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

import java.math.BigInteger;
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
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

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
    def.setType("CEFInInterceptor");
    def.setName("CEFInInterceptor");
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
    boolean isBackChannel = SoapUtils.isRequestMessage(msg);

    SEDBox inboxSb = SoapUtils.getMSHInMailReceiverBox(msg);

    MSHOutMail mOutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);
    if (mInMail != null) {
      if (CEFConstants.S_SERVICE_CONF_TEST.equals(mInMail.getService())) {
        switch (mInMail.getAction()) {
          case CEFConstants.S_ACTION_CONF_TEST_SUBMIT:
            LOG.formatedWarning(
                    "Got message to submit  service: %s, message id: %s, conversation %s ",
                    mInMail.getService(), mInMail.getMessageId(), mInMail.
                    getConversationId());

            handleSubmiMessage(mInMail, inboxSb);
            break;
          case CEFConstants.S_ACTION_CONF_TEST_DELIVER:
            LOG.formatedWarning(
                    "Got delivery notification for service: %s, message id: %s, conversation %s ",
                    mInMail.getService(), mInMail.getMessageId(), mInMail.
                    getConversationId());
            break;
          case CEFConstants.S_ACTION_CONF_TEST_NOTIFY:
            LOG.formatedWarning(
                    "Got advice of delivery for service: %s, (ref)message: %s, conversation %s ",
                    mInMail.getService(), mInMail.getRefToMessageId(), mInMail.
                    getConversationId());
            break;
          default:
            LOG.logError(String.format(
                    "Action %s for service: %s is not exptected",
                    mInMail.getAction(), mInMail.getService()), null);
        }
      } else {
        fireDeliveryNotification(mInMail, inboxSb);
      }
    } else if (isBackChannel) {
      // !!!!!!!!!!!!!!!!!!!!!!!
      // Receipt notification is handled by OutMailEventListnere
      /* if (mOutMail != null) {
        String type = "Receipt";
        fireAdviceOfDeliveryNotification(mOutMail, inboxSb, type);
      }
       */
      LOG.log("TODO: Fire AdviceOfDelivery to MINDER - for as4 receipt");
    }

    LOG.logEnd(l);
    return true;
  }

  /**
   *
   * @param mInMail
   * @param inboxSb
   */
  private void handleSubmiMessage(MSHInMail mInMail, SEDBox inboxSb) {
    long l = LOG.logStart();

    Properties p = new Properties();
    mInMail.getMSHInProperties().getMSHInProperties().forEach(sp -> {
      p.put(sp.getName(), sp.getValue());
    });

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getInstance().getGuidString());

    try {
      setValue(CEFConstants.S_SERVICE_PROP, p, mout);
      setValue(CEFConstants.S_REF_MESSAGE_ID, p, mout);
      setValue(CEFConstants.S_MESSAGE_ID, p, mout);
      setValue(CEFConstants.S_ACTION_PROP, p, mout);
      setValue(CEFConstants.S_CONV_ID, p, mout);
      setValue(CEFConstants.S_TO_PARTY_ID_PROP, p, mout);
      setValue(CEFConstants.S_FROM_PARTY_ID_PROP, p, mout);
      setValue(CEFConstants.S_TO_PARTY_ROLE_PROP, p, mout);
      setValue(CEFConstants.S_FROM_PARTY_ROLE_PROP, p, mout);
    } catch (PModeException ex) {
      LOG.logError("Error creating message", ex);
      return;
    }

    mout.setMSHOutProperties(new MSHOutProperties());

    for (String key : p.stringPropertyNames()) {
      MSHOutProperty mop = new MSHOutProperty();

      mop.setName(key);
      mop.setValue(p.getProperty(key));
      mout.getMSHOutProperties().getMSHOutProperties().add(mop);
    }

    mout.setMSHOutPayload(new MSHOutPayload());
    for (MSHInPart mip : mInMail.getMSHInPayload().getMSHInParts()) {
      MSHOutPart mop = new MSHOutPart();
      mop.setType(mip.getType());
      mop.setEbmsId(mip.getEbmsId());
      mop.setDescription(mip.getDescription());
      mop.setEncoding(mip.getEncoding());
      mop.setFilename(mip.getFilename());
      mop.setFilepath(mip.getFilepath());
      mop.setMimeType(mip.getMimeType());
      mop.setName(mip.getName());
      mop.setSha256Value(mip.getSha256Value());
      mop.setSize(mip.getSize());
      mout.getMSHOutPayload().getMSHOutParts().add(mop);
    }

    try {
      mDB.serializeOutMail(mout, "", "CEFInInterceptor", "");
    } catch (StorageException ex) {
      LOG.logError(l, ex);
    }
    LOG.logEnd(l);
  }

  private void setValue(String prpName, Properties p, MSHOutMail mo)
          throws PModeException {
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
        case CEFConstants.S_MESSAGE_ID:
          mo.setMessageId(val);
          break;
        case CEFConstants.S_REF_MESSAGE_ID:
          mo.setRefToMessageId(val);
          break;
        case CEFConstants.S_CONV_ID:
          mo.setConversationId(val);
          break;
        case CEFConstants.S_TO_PARTY_ID_PROP: {
          LOG.formatedlog("Get party for id %s", val);
          PartyIdentitySet pis = mPMode.getPartyIdentitySetForPartyId(
                  "urn:oasis:names:tc:ebcore:partyid-type:unregistered", val);

          mo.setReceiverName(val);
          mo.setReceiverEBox(val + "@" + pis.getDomain());

        }
        break;
        case CEFConstants.S_FROM_PARTY_ID_PROP:
          mo.setSenderName(val);
          mo.setSenderEBox(val + "@" + SEDSystemProperties.getLocalDomain());
          break;
        case CEFConstants.S_FROM_PARTY_ROLE_PROP:
          break;

      }

    } else {
      LOG.formatedWarning("In propertey %s not exists", prpName);
    }

  }

  public void fireDeliveryNotification(MSHInMail mInMail, SEDBox sbInbox) {
    long l = LOG.logStart();

    // sender
    // receiver
    MSHOutMail mout = new MSHOutMail();
    mout.setMSHOutProperties(new MSHOutProperties());
    mout.setMessageId(Utils.getInstance().getGuidString());

    mout.setService(CEFConstants.S_SERVICE_CONF_TEST_ID);
    mout.setAction(CEFConstants.S_ACTION_CONF_TEST_DELIVER);
    mout.setConversationId(mInMail.getConversationId());
    mout.setSenderName(mInMail.getReceiverName());
    mout.setSenderEBox(mInMail.getReceiverEBox());
    mout.setReceiverName(CEFConstants.S_MINDER_NAME);
    mout.setReceiverEBox(CEFConstants.S_MINDER_ADDRESS);

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_SERVICE_PROP, mInMail.getService()));

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_ACTION_PROP, mInMail.getAction()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_CONV_ID, mInMail.getConversationId()));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_FROM_PARTY_ID_PROP, mInMail.getSenderEBox().
                    substring(0,
                            mInMail.getSenderEBox().indexOf("@"))));
    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_TO_PARTY_ID_PROP, mInMail.getReceiverEBox().
                    substring(0,
                            mInMail.getReceiverEBox().indexOf("@"))));

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_REF_MESSAGE_ID, mInMail.getRefToMessageId()));

    mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
            CEFConstants.S_MESSAGE_ID, mInMail.getMessageId()));

    // add payload
    mout.setMSHOutPayload(new MSHOutPayload());

    for (MSHInPart mip : mInMail.getMSHInPayload().getMSHInParts()) {
      MSHOutPart mop = new MSHOutPart();
      mop.setEbmsId(mip.getEbmsId());
      mop.setType(mip.getType());
      mop.setDescription(mip.getDescription());
      mop.setEncoding(mip.getEncoding());
      mop.setFilename(mip.getFilename());
      mop.setFilepath(mip.getFilepath());
      mop.setMimeType(mip.getMimeType());
      mop.setName(mip.getName());
      mop.setSha256Value(mip.getSha256Value());
      mop.setSize(mip.getSize());

      mout.getMSHOutPayload().getMSHOutParts().add(mop);

    }

    try {
      mDB.serializeOutMail(mout, "", "CEFInInterceptor", "");
    } catch (StorageException ex) {
      LOG.logError(l, ex);
    }

    LOG.logEnd(l);
  }

  /**
   * Method submits mail of succesful deliery
   *
   * @param mOutMail
   * @param sbInbox
   * @param type / public void fireAdviceOfDeliveryNotification(MSHOutMail
   * mOutMail, SEDBox sbInbox, String type) { long l = LOG.logStart();
   *
   * // sender // receiver MSHOutMail mout = new MSHOutMail();
   * mout.setMSHOutProperties(new MSHOutProperties());
   * mout.setMessageId(Utils.getInstance().getGuidString());
   *
   * mout.setService(CEFConstants.S_SERVICE_CONF_TEST_ID);
   * mout.setAction(CEFConstants.S_ACTION_CONF_TEST_NOTIFY);
   * mout.setConversationId(mOutMail.getConversationId());
   * mout.setRefToMessageId(mOutMail.getMessageId());
   * mout.setSenderName(mOutMail.getSenderName());
   * mout.setSenderEBox(mOutMail.getSenderEBox());
   * mout.setReceiverName(CEFConstants.S_MINDER_NAME);
   * mout.setReceiverEBox(CEFConstants.S_MINDER_ADDRESS);
   *
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_SERVICE_PROP, mOutMail.getService()));
   *
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_ACTION_PROP, mOutMail.getAction()));
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_CONV_ID, mOutMail.getConversationId()));
   *
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_FROM_PARTY_ID_PROP, mOutMail.getSenderEBox().substring(0,
   * mOutMail.getSenderEBox().indexOf("@"))));
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_TO_PARTY_ID_PROP, mOutMail.getReceiverEBox().substring(0,
   * mOutMail.getReceiverEBox().indexOf("@"))));
   *
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_REF_MESSAGE_ID, mOutMail.getMessageId()));
   * mout.getMSHOutProperties().getMSHOutProperties().add(createMSHOutProperty(
   * CEFConstants.S_SIGNAL_TYPE_ID, type));
   *
   * try { mDB.serializeOutMail(mout, "", "CEFInInterceptor", ""); } catch
   * (StorageException ex) { LOG.logError(l, ex); }
   *
   * LOG.logEnd(l); }
   */
  private MSHOutProperty createMSHOutProperty(String prpName, String val) {
    MSHOutProperty mop = new MSHOutProperty();
    mop.setName(prpName);
    mop.setValue(val);
    return mop;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t, Properties contextProperties) {
    // ignore
  }

}
