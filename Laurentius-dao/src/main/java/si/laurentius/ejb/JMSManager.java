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
package si.laurentius.ejb;

import java.util.Objects;
import javax.ejb.AccessTimeout;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.management.JMSManagementHelper;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_PREFIX;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.enums.JMSArtemisAttribute;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(JMSManagerInterface.class)
@AccessTimeout(value = 10000)
public class JMSManager implements JMSManagerInterface {

  private static final SEDLogger LOG = new SEDLogger(JMSManager.class);

  private static final String MSG_ERR_CLOSEJMS = "Error occured while closing JMS session: %s.";

  /**
   *
   * @param con
   */
  protected void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (JMSException jmse) {
      LOG.formatedWarning(MSG_ERR_CLOSEJMS, jmse.getMessage());

    }
  }

  /**
   *
   * @param inId
   *
   * @return
   * @throws NamingException
   * @throws javax.jms.JMSException
   */
  @Override
  public boolean exportInMail(long inId)
          throws NamingException, JMSException {

    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_IN_MAIL_PROCESS;

    InitialContext ic = new InitialContext();
    ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);

    Queue queue = (Queue) ic.lookup(msgQueueJndiName);
    try (Connection connection = cf.createConnection();
            Session session = connection.
                    createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(queue)) {
      Message message = session.createMessage();

      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, inId);
      sender.send(message);

    }

    try {
      ic.close();
    } catch (NamingException ignore) {
      LOG.formatedWarning(MSG_ERR_CLOSEJMS, ignore.
              getMessage());
    }

    return true;
  }

  private String getJNDIPrefix() {

    return System.getProperty(SYS_PROP_JNDI_PREFIX, "java:/jboss/");
  }

  private String getJNDI_JMSPrefix() {
    return System.getProperty(SYS_PROP_JNDI_JMS_PREFIX, "java:/jms/");
  }

  /**
   *
   * @param biPosiljkaId
   * @param retry
   * @param delay
   * @param transacted
   * @return
   * @throws NamingException
   * @throws JMSException
   */
  @Override
  public boolean sendMessage(long biPosiljkaId, int retry, long delay,
          boolean transacted) throws NamingException, JMSException {

    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;

    InitialContext ic = new InitialContext();
    ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
    Queue queue = (Queue) ic.lookup(msgQueueJndiName);
   

    try (Connection connection = cf.createConnection(); Session session = connection.createSession(transacted,
            Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(queue);) {
      Message message = session.createMessage();
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, biPosiljkaId);
      message.setStringProperty(
              SEDValues.EBMS_QUEUE_DUPLICATE_DETECTION_ID_Artemis,
              "" +Long.toString(biPosiljkaId));

      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, retry);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, delay);
      message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_AMQ, delay);
      message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_Artemis, System.
              currentTimeMillis()
              + delay);

      LOG.formatedDebug("Submit mail to queue: %d, retry %d, delay %d ",
              biPosiljkaId, retry, delay);
      sender.send(message);

    }

    try {
      ic.close();
    } catch (NamingException ignore) {
      LOG.formatedWarning(MSG_ERR_CLOSEJMS, ignore.
              getMessage());
    }

    return true;
  }

  /**
   *
   * @param queJNDI
   * @return @throws NamingException
   * @throws JMSException
   */
  @Override
  public QueueData getMessageProperties(String queJNDI) throws NamingException, JMSException {
    long l = LOG.logStart(queJNDI);
    InitialContext ic = null;
    Connection connection = null;
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    QueueData res = null;
    try {
      ic = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      connection = cf.createConnection();
      try {
        res = getQueueData(connection, queJNDI);
      } catch (Exception ex) {
        LOG.logError(queJNDI, ex);
      }

    } finally {
      if (ic != null) {
        try {
          ic.close();
        } catch (Exception ignore) {
          LOG.formatedWarning(MSG_ERR_CLOSEJMS, ignore.
                  getMessage());
        }
      }
      closeConnection(connection);
    }
    LOG.logEnd(l, queJNDI);
    return res;
  }

  // To do this we send a management message to get the message count.
  // In real life you wouldn't create a new session every time you send a management message
  private QueueData getQueueData(final Connection connection,
          final String jndiQue) throws JMSException {
    long l = LOG.logStart(jndiQue);
    QueueData qd = new QueueData();

    QueueSession session = ((QueueConnection) connection).createQueueSession(
            false, Session.AUTO_ACKNOWLEDGE);

    Queue managementQueue = ActiveMQJMSClient.createQueue("activemq.management");

    QueueRequestor requestor = new QueueRequestor(session, managementQueue);

    connection.start();
    String queName = ResourceNames.JMS_QUEUE + jndiQue.
            substring(jndiQue.indexOf('/') + 1);

    Message m = session.createMessage();

    qd.setAddress(getQueueAttribute(JMSArtemisAttribute.Address,
            requestor, queName, m));
    qd.setConsumerCount(getQueueAttribute(
            JMSArtemisAttribute.ConsumerCount,
            requestor, queName, m));
    qd.setDeadLetterAddress(getQueueAttribute(
            JMSArtemisAttribute.DeadLetterAddress,
            requestor, queName, m));
    qd.setExpiryAddress(getQueueAttribute(
            JMSArtemisAttribute.ExpiryAddress,
            requestor, queName, m));
    qd.setMessageCount(getQueueAttribute(
            JMSArtemisAttribute.MessageCount, requestor, queName, m));
    qd.setMessagesAdded(getQueueAttribute(
            JMSArtemisAttribute.MessagesAdded,
            requestor, queName, m));

    qd.setScheduledCount(getQueueAttribute(
            JMSArtemisAttribute.ScheduledCount,
            requestor, queName, m));
    LOG.logEnd(l, jndiQue);
    return qd;
  }

  private String getQueueAttribute(JMSArtemisAttribute jmsAtt,
          QueueRequestor requestor, String queName, Message m) {
    Object res = null;
    try {
      JMSManagementHelper.putAttribute(m, queName, jmsAtt.getName());
      Message response = requestor.request(m);
      res = JMSManagementHelper.getResult(response);
      if (res != null && !Objects.equals(res.getClass(), jmsAtt.getValueClass())) {

        LOG.logError(
                String.format(
                        "Error occured while retrieving parameter %s for queue %s. "
                        + "Result %s class:%s, expected: %s",
                        jmsAtt.getName(), queName, res, res.
                        getClass().getName(), jmsAtt.getValueClass()),
                null);

      }
    } catch (Exception ex) {
      LOG.logError(
              String.format(
                      "Error retrieving parameter %s for queue %s. Error: %s.",
                      jmsAtt.getName(), queName, ex.getMessage()), ex);
    }
    return res != null ? res.toString() : "Null";
  }

  public <T> T getNullFormClass(Class c) {
    assert c != null;
    if (c.equals(String.class)) {
      return (T) "";
    } else if (c.equals(int.class)) {
      return (T) Integer.valueOf(0);
    } else if (c.equals(long.class)
            || c.equals(Long.class)) {
      return (T) Long.valueOf(0);
    } else if (c.equals(boolean.class)) {
      return (T) Boolean.FALSE;
    }
    return null;

  }

  @Override
  public boolean pauseQueue(String jndi) throws JMSException, NamingException {
    return invokeJMSQeueOperation(jndi, "pause");
  }

  @Override
  public boolean resumeQueue(String jndi) throws JMSException, NamingException {
    return invokeJMSQeueOperation(jndi, "resume");

  }

  private boolean invokeJMSQeueOperation(
          final String jndiQue, final String operation) throws JMSException, NamingException {

    boolean suc = false;
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;

    InitialContext ic = new InitialContext();
    ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
   
    try ( Connection connection = cf.createConnection();QueueSession session = ((QueueConnection) connection).
            createQueueSession(
                    false, Session.AUTO_ACKNOWLEDGE)) {

      Queue managementQueue = ActiveMQJMSClient.createQueue(
              "activemq.management");

      QueueRequestor requestor = new QueueRequestor(session, managementQueue);

      connection.start();

      Message m = session.createMessage();
      JMSManagementHelper.putOperationInvocation(m,
              ResourceNames.JMS_QUEUE + jndiQue.
                      substring(jndiQue.indexOf('/') + 1), operation);
      Message response = requestor.request(m);
      suc = JMSManagementHelper.hasOperationSucceeded(response);
      if (!suc) {
        Object oRes;
        try {
          oRes = JMSManagementHelper.getResult(response);
          LOG.formatedWarning("Operation %s not succeeded. Message %s",
                  operation, oRes);
        } catch (Exception ex) {
          LOG.logError(
                  "Error retrieving JMS Control message for operation: " + operation,
                  ex);
        }
      }
    } catch (Exception ex) {
      LOG.logError(jndiQue, ex);
    }

    try {
      ic.close();
    } catch (NamingException ignore) {
      LOG.formatedWarning(MSG_ERR_CLOSEJMS, ignore.
              getMessage());
    }

    return suc;
  }

}
