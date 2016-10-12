/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import si.laurentius.commons.SEDInboxMailStatus;

/**
 *
 * @author Jože Rihtaršič
 */
public class InMailTableFilter {

  /**
   *
   */
  protected String action;

  /**
   *
   */
  protected String conversationId;
  
  /**
   *
   */
  protected BigInteger id;
  /**
   *
   */
  protected String messageId;

  /**
   *
   */
  protected Date receivedDateFrom;

  /**
   *
   */
  protected Date receivedDateTo;

  
   /**
   *
   */
  protected Date statusDateFrom;

  /**
   *
   */
  protected Date statusDateTo;

  /**
   *
   */
  protected List<String> receiverEBoxList = new ArrayList<>();
  protected String refToMessageId;

  /**
   *
   */
  protected String senderEBox;
  protected String senderMessageId;

  /**
   *
   */
  protected String service;

  /**
   *
   */
  protected List<String> statusList = new ArrayList<>();

  /**
   *
   */
  protected String subject;

  public InMailTableFilter() {
     for (SEDInboxMailStatus st: SEDInboxMailStatus.values()){
       statusList.add(st.getValue());
     }
  }

  
  
  
  
  /**
   *
   * @return
   */
  public String getAction() {
    return action;
  }

  /**
   *
   * @return
   */
  public String getConversationId() {
    return conversationId;
  }
  public BigInteger getId() {
    return id;
  }
  public String getMessageId() {
    return messageId;
  }

  /**
   *
   * @return
   */
  public Date getReceivedDateFrom() {
    return receivedDateFrom;
  }

  /**
   *
   * @return
   */
  public Date getReceivedDateTo() {
    return receivedDateTo;
  }

  /**
   *
   * @return
   */
  public List<String> getReceiverEBoxList() {
    return receiverEBoxList;
  }
  public String getRefToMessageId() {
    return refToMessageId;
  }

  /*
   * public void setReceiverEBox(String receiverEBox) { this.receiverEBox = receiverEBox; }
   */
  /**
   *
   * @return
   */
  public String getSenderEBox() {
    return senderEBox;
  }
  public String getSenderMessageId() {
    return senderMessageId;
  }

  /**
   *
   * @return
   */
  public String getService() {
    return service;
  }

  /**
   *
   * @return
   */
  public List<String> getStatusList() {
    return statusList;
  }

  /**
   *
   * @return
   */
  public String getSubject() {
    return subject;
  }

  /**
   *
   * @param action
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   *
   * @param conversationId
   */
  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }
  public void setId(BigInteger id) {
    this.id = id;
  }
  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  /**
   *
   * @param receivedDateFrom
   */
  public void setReceivedDateFrom(Date receivedDateFrom) {
    this.receivedDateFrom = receivedDateFrom;
  }

  /**
   *
   * @param receivedDateTo
   */
  public void setReceivedDateTo(Date receivedDateTo) {
    this.receivedDateTo = receivedDateTo;
  }
  public void setRefToMessageId(String refToMessageId) {
    this.refToMessageId = refToMessageId;
  }

  /**
   *
   * @param senderEBox
   */
  public void setSenderEBox(String senderEBox) {
    this.senderEBox = senderEBox;
  }
  public void setSenderMessageId(String senderMessageId) {
    this.senderMessageId = senderMessageId;
  }

  /**
   *
   * @param service
   */
  public void setService(String service) {
    this.service = service;
  }

  /**
   *
   * @param status
   */
  public void setStatusList(List<String> status) {
    this.statusList = status;
  }

  /**
   *
   * @param subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  public Date getStatusDateFrom() {
    return statusDateFrom;
  }

  public void setStatusDateFrom(Date statusDateFrom) {
    this.statusDateFrom = statusDateFrom;
  }

  public Date getStatusDateTo() {
    return statusDateTo;
  }

  public void setStatusDateTo(Date statusDateTo) {
    this.statusDateTo = statusDateTo;
  }
  
  

  @Override
  public String toString() {
    return "InMailTableFilter{" + "action=" + action + ", conversationId=" + conversationId + ", id=" +
        id + ", messageId=" + messageId + ", receivedDateFrom=" + receivedDateFrom +
        ", receivedDateTo=" + receivedDateTo + ", receiverEBoxList=" + receiverEBoxList +
        ", refToMessageId=" + refToMessageId + ", senderEBox=" + senderEBox + ", senderMessageId=" +
        senderMessageId + ", service=" + service + ", status=" + String.join(";", statusList) + ", subject=" + subject +
        '}';
  }


}
