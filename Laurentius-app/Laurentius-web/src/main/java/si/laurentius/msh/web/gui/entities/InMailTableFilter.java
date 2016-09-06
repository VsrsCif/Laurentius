/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.web.gui.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author sluzba
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
  protected Date receivedDateFrom;

  /**
     *
     */
  protected Date receivedDateTo;

  /**
     *
     */
  protected List<String> receiverEBoxList = new ArrayList<>();

  /**
     *
     */
  protected String senderEBox;

  /**
     *
     */
  protected String service;

  /**
     *
     */
  protected String status;

  /**
     *
     */
  protected String subject;

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
  public String getStatus() {
    return status;
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

  /**
   *
   * @param senderEBox
   */
  public void setSenderEBox(String senderEBox) {
    this.senderEBox = senderEBox;
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
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   *
   * @param subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

}
