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
public enum SecHashFunction {
  SHA1("http://www.w3.org/2001/04/xmlenc#sha1", "SHA1"),
  SHA256("http://www.w3.org/2001/04/xmlenc#sha256", "SHA256"),
  SHA512("http://www.w3.org/2001/04/xmlenc#sha512", "SHA512");

  String mstrVal;
  String mstrDesc;

  private SecHashFunction(String val, String strDesc) {
    mstrVal = val;
    mstrDesc = strDesc;

  }

  /**
   *
   * @return
   */
  public String getValue() {

    return mstrVal;
  }

  /**
   *
   * @return
   */
  public String getDesc() {
    return mstrDesc;
  }
  public static SecHashFunction getByValue(String value){
    for(SecHashFunction pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
