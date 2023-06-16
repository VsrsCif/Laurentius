/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp;

import java.util.Date;

/**
 *
 * @author sluzba
 */
public class ZKPMailFilter {
  
    String  status;
    String  senderEBox;
    String  receiverEBox;
    String  action;
    String  service;
    Date sentDateTo;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSenderEBox() {
    return senderEBox;
  }

  public void setSenderEBox(String senderEBox) {
    this.senderEBox = senderEBox;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public Date getSentDateTo() {
    return sentDateTo;
  }

  public void setSentDateTo(Date sentDate) {
    this.sentDateTo = sentDate;
  }

  public String getReceiverEBox() {
    return receiverEBox;
  }

  public void setReceiverEBox(String receiverEBox) {
    this.receiverEBox = receiverEBox;
  }
}
