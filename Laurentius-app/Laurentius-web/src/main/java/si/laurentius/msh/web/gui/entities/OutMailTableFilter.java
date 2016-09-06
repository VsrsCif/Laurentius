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
public class OutMailTableFilter {

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
  protected String receiverEBox;

  /**
     *
     */
  protected List<String> senderEBoxList = new ArrayList<>();

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
     */
  protected Date submittedDateFrom;

  /**
     *
     */
  protected Date submittedDateTo;

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
  public String getReceiverEBox() {
    return receiverEBox;
  }

  /**
   *
   * @return
   */
  public List<String> getSenderEBoxList() {
    return senderEBoxList;
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
   * @return
   */
  public Date getSubmittedDateFrom() {
    return submittedDateFrom;
  }

  /**
   *
   * @return
   */
  public Date getSubmittedDateTo() {
    return submittedDateTo;
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
   * @param receiverEBox
   */
  public void setReceiverEBox(String receiverEBox) {
    this.receiverEBox = receiverEBox;
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
   * @param st
   */
  public void setStatus(String st) {
    this.status = st;
  }

  /**
   *
   * @param subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   *
   * @param submittedDateFrom
   */
  public void setSubmittedDateFrom(Date submittedDateFrom) {
    this.submittedDateFrom = submittedDateFrom;
  }

  /**
   *
   * @param submittedDateTo
   */
  public void setSubmittedDateTo(Date submittedDateTo) {
    this.submittedDateTo = submittedDateTo;
  }

}
