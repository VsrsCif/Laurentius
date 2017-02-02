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
package si.laurentius.commons.interfaces;

import javax.ejb.Local;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.exception.SEDSecurityException;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDCertStoreInterface {

  public static final String KEYSTORE_NAME = "keystore";
  public static final String ROOTCA_NAME = "rootCA";

  void refreshCrlLists();

  SEDCertStore getCertificateStore() throws SEDSecurityException;

  void updateKeystoreCertificate(SEDCertificate crt) throws SEDSecurityException;

  SEDCertStore getRootCACertificateStore() throws SEDSecurityException;
}
