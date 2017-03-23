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
public enum ActionRole {
  Initiator("initiator"),
  Executor("executor");

  String mstrVal;

  private ActionRole(String val) {
    mstrVal = val;
  }

  /**
   *
   * @return
   */
  public String getValue() {

    return mstrVal;
  }

  public static ActionRole getByValue(String value){
    for(ActionRole pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
