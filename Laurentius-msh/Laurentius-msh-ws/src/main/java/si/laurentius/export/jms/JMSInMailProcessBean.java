/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.laurentius.export.jms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;


import si.laurentius.ebox.SEDBox;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.process.SEDProcessorSet;

/**
 *
 * @author Jože Rihtaršič
 */
@MessageDriven(
        activationConfig = {
          @ActivationConfigProperty(propertyName = "acknowledgeMode",
                  propertyValue = "Auto-acknowledge")
          ,
      @ActivationConfigProperty(propertyName = "destinationType",
                  propertyValue = "javax.jms.Queue")
          ,
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/MSHInMailProcessQueue")
          ,
      @ActivationConfigProperty(propertyName = "maxSession",
                  propertyValue = "5")})
@TransactionManagement(TransactionManagementType.BEAN)
public class JMSInMailProcessBean implements MessageListener {

  /**
   *
   */
  public static final SEDLogger LOG = new SEDLogger(JMSInMailProcessBean.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  @Override
  public void onMessage(Message msg) {
    long t = LOG.logStart();
    // parse JMS Message data 
    long jmsMessageId; // 

    // Read property Mail ID
    try {
      jmsMessageId = msg.getLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID);
    } catch (JMSException ex) {
      LOG.logError(t, String.format(
              "Bad JMS message for queue: 'MSHQueue' with no property: '%s'",
              SEDValues.EBMS_QUEUE_PARAM_MAIL_ID), ex);
      return;
    }

    MSHInMail mail;
    try {
      mail = mDB.getMailById(MSHInMail.class, BigInteger.valueOf(jmsMessageId));
    } catch (NoResultException ex) {
      LOG.logError(t,
              "Message with id: '" + jmsMessageId + "' not exists in DB!", ex);
      return;
    }

    SEDBox sb = msedLookup.getSEDBoxByAddressName(mail.getReceiverEBox());
    if (sb == null) {
      String errMsg = String.format(
              "Export failed! Receiver box '%s' not exists",
              mail.getReceiverEBox());
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, errMsg);
      LOG.logError(t,
              "Message with id: '" + jmsMessageId + "' export failed!" + errMsg,
              null);
      return;
    }

    // get procesSets
    List<SEDProcessorSet> lst = getProcessSetsForMail(mail);

    boolean bContinue = true;
    for (SEDProcessorSet sp : lst) {
      // properties
      Map<String, Object> mp = new HashMap<>();
      for (SEDProcessorInstance spi : sp.getSEDProcessorInstances()) {
        InMailProcessorDef mi = mPluginManager.getInMailProcessor(spi.
                getPlugin(), spi.getType());
        InMailProcessorInterface ipi;
        try {
          ipi = InitialContext.doLookup(mi.getJndi());
          bContinue = ipi.proccess(spi.getInstance(), mail, mp);
          if (!bContinue) {
            break;
          }
          LOG.formatedWarning("plugin Jndi  %s", mi.getJndi());
        } catch (NamingException ex) {
          LOG.logError(t,
                  "Message with id: '" + jmsMessageId + "' export failed!" + ex.
                          getMessage(), ex);
        }

      }
      if (!bContinue) {
        break;
      }

    }

    LOG.logEnd(t, jmsMessageId);
  }

  
  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status,
          String msg) {
    try {
      mDB.setStatusToInMail(mail, status, msg);
    } catch (StorageException ex) {
      LOG.logError(
              "Failed to set status to message with id: '" + mail.getId() + "'!",
              ex);
    }
  }

  
  public List<SEDProcessorSet> getProcessSetsForMail(MSHInMail mi) {
    List<SEDProcessorRule> lst = msedLookup.getSEDProcessorRules();
    List<SEDProcessorSet> lstResult = new ArrayList<>();
    for (SEDProcessorRule r : lst) {

      if (r.getService() != null && !r.getService().equals(mi.getService())) {
        continue;
      }
      if (r.getAction() != null && !r.getAction().equals(mi.getAction())) {
        continue;
      }
      if (r.getReceiverEBox() != null && !r.getReceiverEBox().equals(mi.
              getReceiverEBox())) {
        continue;
      }
      if (r.getSenderEBox() != null && !r.getSenderEBox().equals(mi.
              getSenderEBox())) {
        continue;
      }

      lstResult.add(msedLookup.getSEDProcessorSet(r.getProcSetCode()));

    }
    return lstResult;

  }
}
