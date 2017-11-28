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
public enum SecEncryptionDigest {
  DIGEST_SHA1("http://www.w3.org/2001/04/xmlenc#sha1", "DIGEST_SHA1"), 
  DIGEST_SHA256("http://www.w3.org/2001/04/xmlenc#sha256", "DIGEST_SHA256"),  
  DIGEST_SHA512("http://www.w3.org/2001/04/xmlenc#sha512", "DIGEST_SHA512"),
  ;

  String mstrVal;
  String mstrDesc;
  

  private SecEncryptionDigest(String val, String strDesc) {
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

  public static SecEncryptionDigest getByValue(String value) {
    for (SecEncryptionDigest pr :values()) {
      if (Objects.equals(pr.getValue(), value)) {
        return pr;
      }
    }
    return null;

  }

}
