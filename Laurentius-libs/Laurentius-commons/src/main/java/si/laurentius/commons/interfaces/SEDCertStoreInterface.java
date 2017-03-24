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

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.ejb.Local;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.commons.exception.SEDSecurityException;

/**
 *
 * @author Jože Rihtaršič
 */
@Local
public interface SEDCertStoreInterface {

  public static final String KEYSTORE_NAME = "keystore";
  public static final String ROOTCA_NAME = "rootCA";

  public void addCertToCertStore(X509Certificate crt, String alias) throws SEDSecurityException;

  public void addCertToRootCA(X509Certificate crt, String alias) throws SEDSecurityException;

  public void addKeyToToCertStore(String alias, Key privateKey,
          Certificate[] certs, String passwd)
          throws SEDSecurityException;

  public void addPassword(String alias, String pswd) throws SEDSecurityException;
  void changeAlias(String oldAlias, String newAlias) throws SEDSecurityException;
  void changeKeystorePassword(String newPasswd) throws SEDSecurityException;
  void changeRootCAAlias(String oldAlias, String newAlias) throws SEDSecurityException;
  void changeRootCAPassword(String newPasswd) throws SEDSecurityException;
  List<SEDCertificate> getCertificates() throws SEDSecurityException;
  KeyStore getCertStore() throws SEDSecurityException;
  public SEDCertPassword getKeyPassword(String alias);
  PrivateKey getPrivateKeyForAlias(String alias) throws SEDSecurityException;
  PrivateKey getPrivateKeyForX509Cert(X509Certificate xrc) throws SEDSecurityException;
  List<String> getKeystoreAliases(boolean onlyKeys);
  List<X509Certificate> getRootCA509Certs() throws SEDSecurityException;
  List<SEDCertificate> getRootCACertificates() throws SEDSecurityException;
  List<SEDCertCRL> getSEDCertCRLs();
  SEDCertificate getSEDCertificatForAlias(String alias);
  X509Certificate getX509CertForAlias(String alias) throws SEDSecurityException;
  void refreshCrlLists();
  void removeCertificateFromRootCAStore(SEDCertificate crt) throws SEDSecurityException;
  void removeCertificateFromStore(SEDCertificate crt) throws SEDSecurityException;


}
