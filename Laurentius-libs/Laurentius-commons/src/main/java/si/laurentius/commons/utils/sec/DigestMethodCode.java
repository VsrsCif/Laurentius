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
package si.laurentius.commons.utils.sec;

/**
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public enum DigestMethodCode {
  SHA1("SHA-1", "http://www.w3.org/2000/09/xmldsig#sha1"),
  SHA256("SHA-256", "http://www.w3.org/2001/04/xmlenc#sha256"),
  SHA512("SHA-512", "http://www.w3.org/2001/04/xmlenc#sha512"),
  MD5("MD5", "");

  String jcaCode;
  String algorithmURI;

  private DigestMethodCode(String jcaCode, String algorithmURI) {
    this.jcaCode = jcaCode;
    this.algorithmURI = algorithmURI;
  }

  public String getJcaCode() {
    return jcaCode;
  }

  public String getAlgorithmURI() {
    return algorithmURI;
  }

}
