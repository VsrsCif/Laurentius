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
public enum MEPType {
  OneWay("OneWay", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay"),
  TwoWay("TwoWay", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/twoWay");

  String mstrVal;
  String mstrName;

  private MEPType(String name, String val) {
    mstrVal = val;
    mstrName = name;
  }

  /**
   *
   * @return
   */
  public String getValue() {
    return mstrVal;
  }

  public String getName() {
    return mstrName;
  }
  

  public static MEPType getByValue(String value){
    for(MEPType pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
