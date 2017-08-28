/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.payload.MSHOutPart;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDTestDao implements SEDDaoInterface{

  @Override
  public boolean addExecutionTask(SEDTaskExecution ad)
      throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean addInMailPayload(MSHInMail mi, List<MSHInPart> lstParts,
          SEDInboxMailStatus status, String statusdesc, String userId, String applicationId) throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean addOutMailPayload(MSHOutMail mi, List<MSHOutPart> lstParts,
          SEDOutboxMailStatus status,  String statusdesc, String userId, String applicationId) throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> List<T> getDataList(Class<T> type, int startingAt, int maxResultCnt, String sortField,
      String sortOrder, Object filters) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> long getDataListCount(Class<T> type, Object filters) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<MSHInMail> getInMailConvIdAndAction(String action, String convId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDTaskExecution getLastSuccesfullTaskExecution(BigInteger cronId, String type)
      throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> T getMailById(Class<T> type, BigInteger mailId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> List<T> getMailByMessageId(Class<T> type, String mailId) {
    return Collections.emptyList();
  }

  @Override
  public <T> List<T> getMailBySenderMessageId(Class<T> type, String mailSenderId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> List<T> getMailEventList(Class<T> type, BigInteger mailId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> List<T> getMailPartList(Class<T> type, BigInteger mailId) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void removeInMail(BigInteger bi)
      throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void removeOutMail(BigInteger bi)
      throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void sendOutMessage(MSHOutMail mail, int retry, long delay, String userId,
      String applicationId)
      throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void serializeInMail(MSHInMail mail, String applicationId)
      throws StorageException {
    
  }

  @Override
  public void serializeOutMail(MSHOutMail mail, String userID, String applicationId, String pmodeId)
      throws StorageException {
    // no implementation
  }

  @Override
  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc)
      throws StorageException {
    // no implementation
  }

  @Override
  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc,
      String userID, String applicationId)
      throws StorageException {
    // no implementation
  }

  @Override
  public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status, String desc,
      String userID, String applicationId, String filePath, String mime)
      throws StorageException {
  // no implementation
  }

  @Override
  public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc)
      throws StorageException {
    // no implementation
  }

  @Override
  public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc,
      String userID, String applicationId)
      throws StorageException {
    // no implementation
  }

  @Override
  public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status, String desc,
      String userID, String applicationId, String filePath, String mime)
      throws StorageException {
    // no implementation
  }

  @Override
  public boolean updateExecutionTask(SEDTaskExecution ad)
      throws StorageException {
    // no implementation
    return false;
  }

  @Override
  public void updateInMail(MSHInMail mail, String statusDesc, String userID)
      throws StorageException {
    
  }

  @Override
  public void updateOutMail(MSHOutMail mail, String statusDesc, String userID)
      throws StorageException {
    }

  @Override
  public boolean updateOutMailPayload(MSHOutMail mi,
          List<MSHOutPart> lstAddParts, List<MSHOutPart> lstUpdateParts,
          List<MSHOutPart> lstDeleteParts, SEDOutboxMailStatus status,
          String statusdesc, String userId, String applicationId) throws StorageException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
