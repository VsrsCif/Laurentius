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

import java.math.BigInteger;
import java.util.List;
import javax.ejb.Local;
import javax.jms.JMSException;
import javax.naming.NamingException;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.StorageException;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDDaoInterface {

  /**
   *
   * @param <T>
   * @param type
   * @param startingAt
   * @param maxResultCnt
   * @param sortField
   * @param sortOrder
   * @param filters
   * @return
   */
  <T> List<T> getDataList(Class<T> type, int startingAt, int maxResultCnt, String sortField,
      String sortOrder, Object filters);

  /**
   *
   * @param <T>
   * @param type
   * @param filters
   * @return
   */
  <T> long getDataListCount(Class<T> type, Object filters);

  /**
   *
   * @param <T>
   * @param type
   * @param mailId
   * @return
   */
  <T> List<T> getMailEventList(Class<T> type, BigInteger mailId);

  /**
   *
   * @param <T>
   * @param type
   * @param mailId
   * @return
   */
  <T> T getMailById(Class<T> type, BigInteger mailId);
  
  
    /**
   *
   * @param <T>
   * @param type
   * @param mailId
   * @return
   */
  <T> List<T> getMailByMessageId(Class<T> type, String mailId);
  
   /**
   *
   * @param <T>
   * @param type
   * @param mailId
   * @return
   */
  <T> List<T> getMailBySenderMessageId(Class<T> type, String mailSenderId);

  /**
   *
   * @param action
   * @param convId
   * @return
   */
  List<MSHInMail> getInMailConvIdAndAction(String action, String convId);

  /**
   *
   * @param mail
   * @param status
   * @param desc
   */
  void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc)
      throws StorageException;

  /**
   *
   * @param mail
   * @param status
   * @param desc
   * @param userID
   * @param applicationId
   * @throws StorageException
   */
  void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc, String userID,
      String applicationId) throws StorageException;
  
  
  /**
   * 
   * @param mail
   * @param status
   * @param desc
   * @param userID
   * @param applicationId
   * @param filePath
   * @param mime
   * @throws StorageException 
   */
   void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc, String userID,
      String applicationId, String filePath, String mime) throws StorageException;

  /**
   *
   * @param mail
   * @param status
   * @param desc
   */
  void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc)
      throws StorageException;

  /**
   *
   * @param mail
   * @param status
   * @param desc
   * @param userID
   * @param applicationId
   * @throws StorageException
   */
  void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc, String userID,
      String applicationId) throws StorageException;

  
  /**
   * 
   * @param mail
   * @param status
   * @param desc
   * @param userID
   * @param applicationId
   * @param filePath
   * @param mime
   * @throws StorageException 
   */
   void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc, String userID,
      String applicationId, String filePath, String mime) throws StorageException;
  
  
  /**
   *
   * @param mail
   * @param statusDesc
   * @param userID
   * @throws StorageException
   */
  void updateInMail(MSHInMail mail, String statusDesc, String userID) throws StorageException;
  
   /**
   *
   * @param mail
   * @param statusDesc
   * @param userID
   * @throws StorageException
   */
  void updateOutMail(MSHOutMail mail, String statusDesc, String userID) throws StorageException;


  /**
   *
   * @param mail
   * @param userID
   * @param applicationId
   * @param pmodeId
   * @throws StorageException
   */
  void serializeOutMail(MSHOutMail mail, String userID, String applicationId, String pmodeId)
      throws StorageException;
  
  /**
   *
   * @param biPosiljkaId
   * @param strPmodeId
   * @param retry
   * @param delay
   * @param applicationId
   * @throws si.laurentius.commons.exception.StorageException


   * @throws NamingException
   */
  void sendOutMessage(MSHOutMail mail, int retry, long delay, String userId,
      String applicationId) throws StorageException;

  /**
   *
   * @param mail
   * @param applicationId
   * @throws StorageException
   */
  void serializeInMail(MSHInMail mail, String applicationId) throws StorageException;

  /**
   *
   * @param bi
   * @throws StorageException
   */
  void removeInMail(BigInteger bi) throws StorageException;

  /**
   *
   * @param bi
   * @throws StorageException
   */
  void removeOutMail(BigInteger bi) throws StorageException;

  /**
   *
   * @param ad
   * @return
   * @throws StorageException
   */
  boolean addExecutionTask(SEDTaskExecution ad) throws StorageException;

  /**
   *
   * @param ad
   * @return
   * @throws StorageException
   */
  boolean updateExecutionTask(SEDTaskExecution ad) throws StorageException;

  /**
   *
   * @param type
   * @return
   */
  SEDTaskExecution getLastSuccesfullTaskExecution(String type) throws StorageException;
}
