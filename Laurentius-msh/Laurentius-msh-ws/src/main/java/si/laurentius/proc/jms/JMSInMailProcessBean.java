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
package si.laurentius.proc.jms;

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
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.rule.DecisionRuleAssertion;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorRule;

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

  DecisionRuleAssertion mDecRuleAssertion = new DecisionRuleAssertion();

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

    List<SEDProcessor> lstPrc = getProcessSetsForMail(mail);

    LOG.formatedDebug("Got %d processors for mail %d", lstPrc.size(), mail.
            getId());
    boolean bContinue = true;
    try {
      if (lstPrc.size() > 0) {
        boolean setDelivered = false;
        for (SEDProcessor prc : lstPrc) {
          if (!prc.isActive()) {
            continue;
          }
          setDelivered |= prc.isDeliveredOnSuccess();
          Map<String, Object> mp = new HashMap<>();

          for (SEDProcessorInstance spi : prc.getSEDProcessorInstances()) {
            setStatusToInMail(mail, SEDInboxMailStatus.PROCESS, String.format(
                    "Process '%s'(plugin: %s and type %s)", prc.getName(), spi.
                    getPlugin(), spi.getType()));
            LOG.formatedDebug(
                    "Process plugin: %s and type %s for mail %d with ",
                    spi.getPlugin(), spi.getType(), mail.getId());
            bContinue = processMail(spi, mail, mp);

            if (!bContinue) {
              setStatusToInMail(mail, SEDInboxMailStatus.RECEIVED, String.
                      format(
                              "Stopped by %s (plugin: %s and type %s)", prc.
                                      getName(), spi.
                                      getPlugin(), spi.getType()));
              break;
            }

          }

          if (!bContinue) {
            break;
          } else {
            setStatusToInMail(mail,
                    setDelivered ? SEDInboxMailStatus.DELIVERED : SEDInboxMailStatus.RECEIVED,
                    String.format("Processed by %s processor.", prc.getName()));
          }
        }      
      }

    } catch (NamingException ex) {
      String msgErr = "Error discovering processor: " + ex.getMessage();
      LOG.logError(t, msgErr, ex);
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, msgErr);
    } catch (InMailProcessException ex) {
      String msgErr = "Error processing mail: " + ex.getMessage();
      LOG.logError(t, msgErr, ex);
      setStatusToInMail(mail, SEDInboxMailStatus.ERROR, msgErr);
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

  public List<SEDProcessor> getProcessSetsForMail(MSHInMail inMail) {
    assert inMail != null : "inmail is null";

    List<SEDProcessor> lstResult = new ArrayList<>();
    List<SEDProcessor> lstPrcAll = msedLookup.getSEDProcessors();

    boolean process = true;
    for (SEDProcessor prc : lstPrcAll) {
      if (prc.isActive() == null || !prc.isActive()) {
        continue;
      }
      process = true;
      for (SEDProcessorRule pr : prc.getSEDProcessorRules()) {

        if (!mDecRuleAssertion.assertRule(inMail, pr)) {
          process = false;
          break;
        }
      }
      if (process) {
        lstResult.add(prc);
      }
    }
    return lstResult;

  }

  private boolean processMail(SEDProcessorInstance spi, MSHInMail mail,
          Map<String, Object> mp) throws NamingException, InMailProcessException {
    boolean res = true;
    long t = LOG.logStart(mail.getId(), spi.getPlugin(), spi.getType());
    // set properties
    spi.getSEDProcessorProperties().
            forEach((sp) -> {
              mp.put(sp.getKey(), sp.getValue());
            });
    // get proc instance
    InMailProcessorDef mi = mPluginManager.getInMailProcessor(spi.
            getPlugin(), spi.getType());

    // get inMail processor
    InMailProcessorInterface ipi = InitialContext.doLookup(mi.getJndi());
    // process
    res = ipi.proccess(mail, mp);

    LOG.logEnd(t, mail.getId(), spi.getPlugin(), spi.getType());
    return res;
  }
}
