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
  Push("Push", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push", MEPType.OneWay, 1),
  Pull("Pull", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull", MEPType.OneWay, 1),
  Sync("Sync", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/sync", MEPType.TwoWay, 1),
  PushAndPush("PushAndPush", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPush", MEPType.TwoWay, 2),
  PushAndPull("PushAndPull", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull", MEPType.TwoWay, 2),
  PullAndPush("PullAndPush", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pullAndPush", MEPType.TwoWay, 2),
  
  ;
  

  String mstrVal;
  String mstrName;
  MEPType mepType;
  int iLegsNum;

  private MEPChannelBindingType(String name, String val, MEPType mt ,int ln) {
    mstrVal = val;
    mstrName = name;
    mepType = mt;
    iLegsNum = ln;
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
  
  
  
}
