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
package si.laurentius.commons.utils;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_PREFIX;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.interfaces.JMSManagerInterface;

/**
 *
 * @author sluzba
 */
@Stateless
@Local(JMSManagerInterface.class)
public class JMSManager implements JMSManagerInterface {

  private static final SEDLogger LOG = new SEDLogger(JMSManager.class);

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

    }
  }

  /**
   *
   * @param inId
   * @param command
   * @param parameters
   * @return
   * @throws NamingException
   */
  @Override
  public boolean executeProcessOnInMail(long inId, String command, String parameters)
      throws NamingException, JMSException {

    boolean suc = false;
    InitialContext ic = null;
    Connection connection = null;
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EXECUTION;
    try {
      ic = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      Queue queue = (Queue) ic.lookup(msgQueueJndiName);
      connection = cf.createConnection();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer sender = session.createProducer(queue);
      Message message = session.createMessage();

      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, inId);
      message.setStringProperty(SEDValues.EXEC_COMMAND, command);
      message.setStringProperty(SEDValues.EXEC_PARAMS, parameters);
      sender.send(message);
      suc = true;
    } finally {
      if (ic != null) {
        try {
          ic.close();
        } catch (Exception ignore) {
        }
      }
      closeConnection(connection);
    }

    return suc;
  }

  private String getJNDIPrefix() {

    return System.getProperty(SYS_PROP_JNDI_PREFIX, "java:/jboss/");
  }

  /*
   * private String getJNDIPrefix() { return "__/"; // return
   * System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX, "java:/"); }
   */
  private String getJNDI_JMSPrefix() {
    return System.getProperty(SYS_PROP_JNDI_JMS_PREFIX, "java:/jms/");
  }

  /**
   *
   * @param biPosiljkaId
   * @param strPmodeId
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
    boolean suc = false;
    InitialContext ic = null;
    Connection connection = null;
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
    try {
      ic = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      Queue queue = (Queue) ic.lookup(msgQueueJndiName);
      connection = cf.createConnection();
      Session session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
      MessageProducer sender = session.createProducer(queue);
      Message message = session.createMessage();

      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, biPosiljkaId);
      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, retry);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, delay);
      message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_AMQ, delay);
      message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_Artemis, System.currentTimeMillis()
          + delay);

      sender.send(message);
      suc = true;
    } finally {
      if (ic != null) {
        try {
          ic.close();
        } catch (Exception ignore) {
        }
      }
      closeConnection(connection);
    }

    return suc;
  }
}
