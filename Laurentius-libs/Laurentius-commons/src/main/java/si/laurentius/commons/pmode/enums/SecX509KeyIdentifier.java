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
public enum SecX509KeyIdentifier {
  DirectReference("DirectReference", "DirectReference"),
  IssuerSerial("IssuerSerial", "IssuerSerial"),
  X509KeyIdentifier("X509KeyIdentifier", "X509KeyIdentifier"),
  SKIKeyIdentifier("SKIKeyIdentifier", "SKIKeyIdentifier"),
  EmbeddedKeyName("EmbeddedKeyName", "EmbeddedKeyName"),
  Thumbprint("Thumbprint", "Thumbprint"),
  EncryptedKeySHA1("EncryptedKeySHA1", "EncryptedKeySHA1"),
  KeyValue("KeyValue", "KeyValue");
  
  String mstrVal;
  String mstrDesc;

  private SecX509KeyIdentifier(String val, String strDesc) {
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

  public static SecX509KeyIdentifier getByValue(String value) {
    for (SecX509KeyIdentifier pr : values()) {
      if (Objects.equals(pr.getValue(), value)) {
        return pr;
      }
    }
    return null;

  }

}
