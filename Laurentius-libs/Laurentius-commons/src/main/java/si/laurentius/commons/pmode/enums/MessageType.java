/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.pmode.enums;

import java.util.Objects;

/**
 *
 * @author sluzba
 */
public enum MessageType {
  SignalMessage("signalMessage"),
  UserMessage("userMessage");

  String mstrVal;

  private MessageType(String val) {
    mstrVal = val;
  }

  /**
   *
   * @return
   */
  public String getValue() {

    return mstrVal;
  }

  public static MessageType getByValue(String value){
    for(MessageType pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
