/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.laurentius.msh.web.gui.entities;

import java.security.cert.X509Certificate;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDCertificate {

  String alias;
  X509Certificate x509Certificate;

  /**
   *
   * @param alias
   * @param x509Certificate
   */
  public SEDCertificate(String alias, X509Certificate x509Certificate) {
    this.alias = alias;
    this.x509Certificate = x509Certificate;
  }

  /**
   *
   * @return
   */
  public String getAlias() {
    return alias;
  }

  /**
   *
   * @return
   */
  public X509Certificate getX509Certificate() {
    return x509Certificate;
  }

  /**
   *
   * @param alias
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   *
   * @param x509Certificate
   */
  public void setX509Certificate(X509Certificate x509Certificate) {
    this.x509Certificate = x509Certificate;
  }

}
