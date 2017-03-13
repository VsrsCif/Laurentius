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
public enum SecEncryptionAlgorithm {
  AES_128("http://www.w3.org/2009/xmlenc11#aes128-gcm", "AES-128"),
  AES_192("http://www.w3.org/2001/04/xmlenc#aes192-cbc", "AES-192 "),
  AES_256("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES-256"),
  AES128_GCM("http://www.w3.org/2009/xmlenc11#aes128-gcm", "AES128-GCM"),
  AES192_GCM("http://www.w3.org/2009/xmlenc11#aes192-gcm", "AES192-GCM"),
  AES256_GCM("http://www.w3.org/2009/xmlenc11#aes256-gcm", "AES256-GCM"),
  ;

  String mstrVal;
  String mstrDesc;

  private SecEncryptionAlgorithm(String val, String strDesc) {
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


  public static SecEncryptionAlgorithm getByValue(String value){
    for(SecEncryptionAlgorithm pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
