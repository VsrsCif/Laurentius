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
public enum MEPChannelBindingType {
  Push("Push", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push", MEPType.OneWay, 1, true),
  Pull("Pull", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull", MEPType.OneWay, 1, false),
  Sync("Sync", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/sync", MEPType.TwoWay, 1, true),
  PushAndPush("PushAndPush", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPush", MEPType.TwoWay, 2, true),
  PushAndPull("PushAndPull", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull", MEPType.TwoWay, 2, false),
  PullAndPush("PullAndPush", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pullAndPush", MEPType.TwoWay, 2, false),
  
  ;
  

  String mstrVal;
  String mstrName;
  MEPType mepType;
  int iLegsNum;
  boolean bImplemented;

  private MEPChannelBindingType(String name, String val, MEPType mt ,int ln, boolean bi) {
    mstrVal = val;
    mstrName = name;
    mepType = mt;
    iLegsNum = ln;
    bImplemented = bi;
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
  

  public static MEPChannelBindingType getByValue(String value){
    for(MEPChannelBindingType pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }

  public MEPType getMepType() {
    return mepType;
  }

  public int getLegsNum() {
    return iLegsNum;
  }
  
  public boolean isImplemented(){
    return bImplemented;
  }
  
  
}
