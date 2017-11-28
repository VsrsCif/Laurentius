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
public enum SecEncryptionMGFAlgorithm {
  MGF1_SHA1("http://www.w3.org/2009/xmlenc11#mgf1sha1", "MGF1_SHA1"), 
  MGF1_SHA256("http://www.w3.org/2009/xmlenc11#mgf1sha256", "MGF1_SHA256"),  
  MGF1_SHA512("http://www.w3.org/2009/xmlenc11#mgf1sha512", "MGF1_SHA512"),;

  String mstrVal;
  String mstrDesc;
  

  private SecEncryptionMGFAlgorithm(String val, String strDesc) {
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

  public static SecEncryptionMGFAlgorithm getByValue(String value) {
    for (SecEncryptionMGFAlgorithm pr :values()) {
      if (Objects.equals(pr.getValue(), value)) {
        return pr;
      }
    }
    return null;

  }

}
