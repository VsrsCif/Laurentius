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
package si.laurentius.commons.interfaces;

import java.io.Serializable;
import javax.ejb.Local;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface JMSManagerInterface {

  /**
   *
   * @param biPosiljkaId
   * @param strPmodeId
   * @param retry
   * @param delay
   * @param transacted
   * @return
   * @throws NamingException
   */
  boolean sendMessage(long biPosiljkaId, int retry, long delay,
          boolean transacted) throws NamingException, JMSException;

  /**
   *
   * @param biInMailId
   * @return
   * @throws NamingException
   * @throws JMSException
   */
  boolean exportInMail(long biInMailId)
          throws NamingException, JMSException;

  QueueData getMessageProperties(String queName) throws NamingException, JMSException;

  boolean pauseQueue(String queName) throws NamingException, JMSException;

  boolean resumeQueue(String queName) throws NamingException, JMSException;

  public static class QueueData implements Serializable {

    java.lang.String Address;
    java.lang.String  ConsumerCount;
    java.lang.String DeadLetterAddress;
    java.lang.String  DeliveringCount;
    java.lang.String ExpiryAddress;
    java.lang.String  FirstMessageAge;
    java.lang.String  MessageCount;
    java.lang.String  MessagesAcknowledged;
    java.lang.String  MessagesAdded;
    java.lang.String Name;
    java.lang.String  Paused;
    java.lang.String  ScheduledCount;

    public String getAddress() {
      return Address;
    }

    public void setAddress(String Address) {
      this.Address = Address;
    }

    public String getConsumerCount() {
      return ConsumerCount;
    }

    public void setConsumerCount(String ConsumerCount) {
      this.ConsumerCount = ConsumerCount;
    }

    public String getDeadLetterAddress() {
      return DeadLetterAddress;
    }

    public void setDeadLetterAddress(String DeadLetterAddress) {
      this.DeadLetterAddress = DeadLetterAddress;
    }

    public String getDeliveringCount() {
      return DeliveringCount;
    }

    public void setDeliveringCount(String DeliveringCount) {
      this.DeliveringCount = DeliveringCount;
    }

    public String getExpiryAddress() {
      return ExpiryAddress;
    }

    public void setExpiryAddress(String ExpiryAddress) {
      this.ExpiryAddress = ExpiryAddress;
    }

    public String getFirstMessageAge() {
      return FirstMessageAge;
    }

    public void setFirstMessageAge(String FirstMessageAge) {
      this.FirstMessageAge = FirstMessageAge;
    }

    public String getMessageCount() {
      return MessageCount;
    }

    public void setMessageCount(String MessageCount) {
      this.MessageCount = MessageCount;
    }

    public String getMessagesAcknowledged() {
      return MessagesAcknowledged;
    }

    public void setMessagesAcknowledged(String MessagesAcknowledged) {
      this.MessagesAcknowledged = MessagesAcknowledged;
    }

    public String getMessagesAdded() {
      return MessagesAdded;
    }

    public void setMessagesAdded(String MessagesAdded) {
      this.MessagesAdded = MessagesAdded;
    }

    public String getName() {
      return Name;
    }

    public void setName(String Name) {
      this.Name = Name;
    }

    public String getPaused() {
      return Paused;
    }

    public void setPaused(String Paused) {
      this.Paused = Paused;
    }

    public String getScheduledCount() {
      return ScheduledCount;
    }

    public void setScheduledCount(String ScheduledCount) {
      this.ScheduledCount = ScheduledCount;
    }

   
  }
}
