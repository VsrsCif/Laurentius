/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp.entities;

import java.util.Date;

/**
 *
 * @author sluzba
 */
public class LastDeliveredMail {

  String receiverEBox;
  Date deliveredDate;

  public LastDeliveredMail() {
  }

  public LastDeliveredMail(String receiverEBox, Date deliveredDate) {
    this.receiverEBox = receiverEBox;
    this.deliveredDate = deliveredDate;
  }

  public String getReceiverEBox() {
    return receiverEBox;
  }

  public void setReceiverEBox(String receiverEBox) {
    this.receiverEBox = receiverEBox;
  }

  public Date getDeliveredDate() {
    return deliveredDate;
  }

  public void setDeliveredDate(Date deliveredDate) {
    this.deliveredDate = deliveredDate;
  }

}
