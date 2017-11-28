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
public enum SecEncryptionKeyTransport {
  RSA_OAEP("http://www.w3.org/2009/xmlenc11#rsa-oaep", "RSA_OAEP"),
  ;

  String mstrVal;
  String mstrDesc;
  

  private SecEncryptionKeyTransport(String val, String strDesc) {
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

  public static SecEncryptionKeyTransport getByValue(String value) {
    for (SecEncryptionKeyTransport pr :values()) {
      if (Objects.equals(pr.getValue(), value)) {
        return pr;
      }
    }
    return null;

  }

}
