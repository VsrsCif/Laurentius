/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.utils;

/**
 *
 * @author sluzba
 */
public class DisableService {
  String serviceId;
  String receiverBox;
  String senderBox;

  public DisableService(String serviceId, String receiverBox, String senderBox) {
    this.serviceId = serviceId;
    this.receiverBox = receiverBox;
    this.senderBox = senderBox;
  }

 

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getReceiverBox() {
    return receiverBox;
  }

  public void setReceiverBox(String receiverBox) {
    this.receiverBox = receiverBox;
  }

  public String getSenderBox() {
    return senderBox;
  }

  public void setSenderBox(String senderBox) {
    this.senderBox = senderBox;
  }
  
  
  
}
