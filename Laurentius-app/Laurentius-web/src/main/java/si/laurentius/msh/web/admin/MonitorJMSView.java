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
package si.laurentius.msh.web.admin;

import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;
import javax.naming.NamingException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "monitorJMSView")
public class MonitorJMSView extends AbstractJSFView {

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  private static final SEDLogger LOG = new SEDLogger(MonitorJMSView.class);

  public JMSManagerInterface.QueueData getSubmitQueueData() {
    return gettQueueData(SEDValues.JNDI_QUEUE_EBMS);   
  }

  public JMSManagerInterface.QueueData getProcessMessages() {
    return gettQueueData(SEDValues.JNDI_QUEUE_IN_MAIL_PROCESS);
  }
  
  private JMSManagerInterface.QueueData gettQueueData(String queueName) {
    long l = LOG.logStart();
    JMSManagerInterface.QueueData iRes = null;
    try {
      iRes = mJMS.getMessageProperties(queueName);
    } catch (NamingException | JMSException ex) {
      String msg = String.format("Error retrieving %s data. Error: %s.",
              queueName, ex.getMessage());
      LOG.logError(l, msg, ex);
      addError(msg);
    }
    LOG.logEnd(l, queueName);
    return iRes;
  }

  public void pauseSubmitQue() {
    long l = LOG.logStart();
    Map<String, Object> iRes = null;
    try {
      mJMS.pauseQueue(SEDValues.JNDI_QUEUE_EBMS);
    } catch (NamingException | JMSException ex) {
      LOG.logError(l, ex);
    }
  }

  public void resumSubmitQue() {
    long l = LOG.logStart();
    Map<String, Object> iRes = null;
    try {
      mJMS.resumeQueue(SEDValues.JNDI_QUEUE_EBMS);
    } catch (NamingException | JMSException ex) {
      LOG.logError(l, ex);
    }
  }

}
