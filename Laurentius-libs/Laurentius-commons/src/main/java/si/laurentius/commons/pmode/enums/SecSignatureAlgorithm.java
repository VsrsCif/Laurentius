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
public enum SecSignatureAlgorithm {
  RSA_SHA1("http://www.w3.org/2000/09/xmldsig#rsa-sha1", "RSA-SHA1"),
  RSA_SHA256("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "RSA-SHA256"),
  RSA_SHA512("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "RSA-SHA512");

  String mstrVal;
  String mstrDesc;

  private SecSignatureAlgorithm(String val, String strDesc) {
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


  public static SecSignatureAlgorithm getByValue(String value){
    for(SecSignatureAlgorithm pr: values()){
      if (Objects.equals(pr.getValue(), value)){
        return pr;
      }
    }
    return null;
  
  }
  
}
