/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb.entity;

import java.util.Date;

/**
 *
 * @author sluzba
 */
public class TestEntity {

  String receiverEBox;
  Date deliveredDate;

  public TestEntity() {
  }

  public TestEntity(String receiverEBox, Date deliveredDate) {
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
