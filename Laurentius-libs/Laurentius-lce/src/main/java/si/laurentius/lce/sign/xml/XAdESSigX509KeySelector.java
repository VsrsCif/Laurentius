/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.lce.sign.xml;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.*;

/**
 * XAdESSigX509KeySelector returns certificate in Signature/KeyInfo/X509Data/X509Certificate for
 * validating signature
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class XAdESSigX509KeySelector extends KeySelector {

  @Override
  public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose,
      AlgorithmMethod method, XMLCryptoContext context)
      throws KeySelectorException {

    // return null if keyinfo is null or keystore is empty
    if (keyInfo == null) {
      return new SimpleKeySelectorResult(null);
    }
    // Iterate through KeyInfo types
    Iterator i = keyInfo.getContent().iterator();
    while (i.hasNext()) {
      XMLStructure kiType = (XMLStructure) i.next();
      if (kiType instanceof X509Data) {
        X509Data xd = (X509Data) kiType;
        KeySelectorResult ksr = x509DataSelect(xd);
        return ksr;
      }

    }

    // return null since no match could be found
    return new SimpleKeySelectorResult(null);
  }

  /**
   * Searches X509Data for X509Certificate. Expected is only one Certificate. // Insert code here if
   * validation of certificate should be implied
   *
   * @return a KeySelectorResult containing the first cert's public key in X509Data. If no
   * X509Certificate returns null
   */
  private KeySelectorResult x509DataSelect(X509Data xd)
      throws KeySelectorException {

    KeySelectorResult ksr = null;
    Iterator xi = xd.getContent().iterator();
    while (xi.hasNext()) {

      Object o = xi.next();
      if (o instanceof X509Certificate) {
        X509Certificate xcert = (X509Certificate) o;
        ksr = new SimpleKeySelectorResult(xcert.getPublicKey());
        break;
      }
    }
    return ksr;
  }

  /**
   * A simple KeySelectorResult containing a public key.
   */
  private static class SimpleKeySelectorResult implements KeySelectorResult {

    private final Key key;

    SimpleKeySelectorResult(Key key) {
      this.key = key;
    }

    @Override
    public Key getKey() {
      return key;
    }
  }
}
