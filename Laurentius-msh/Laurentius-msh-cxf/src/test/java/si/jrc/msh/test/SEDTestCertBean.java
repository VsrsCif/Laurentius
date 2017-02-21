/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.test;

import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import si.jrc.msh.interceptor.EBMSInInterceptorTest;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDTestCertBean implements SEDCertStoreInterface {


  KeystoreUtils mku = new KeystoreUtils();
  public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
      EBMSInInterceptorTest.class);

  public SEDTestCertBean() {
    /*
    try {
      mCertStore = new SEDCertStore();
      mCertStore.setFilePath("${laurentius.home}/../src/test/resources/security/msh.e-box-a-keystore.jks");
      mCertStore.setPassword("test1234");
      mCertStore.setType("JKS");
      mku.refreshCertStore(mCertStore);
    } catch (SEDSecurityException ex) {
      LOG.error(ex.getMessage(), ex);
    }*/
  }

  @Override
  public void addCertToCertStore(X509Certificate crt, String alias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addCertToRootCA(X509Certificate crt, String alias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addKeyToToCertStore(String alias, Key privateKey,
          Certificate[] certs, String passwd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void addPassword(String alias, String pswd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Properties getCXFKeystoreProperties(String alias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Properties getCXFTruststoreProperties(String alias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public X509KeyManager[] getKeyManagerForAlias(String alias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDCertPassword getKeyPassword(String alias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<X509Certificate> getRootCA509Certs() throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public X509TrustManager getTrustManagerForAlias(String alias,
          boolean validateRootCA) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }



  @Override
  public void refreshCrlLists() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  


  @Override
  public List<String> getKeystoreAliases(boolean onlyKeys) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SEDCertCRL> getSEDCertCRLs() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }


  @Override
  public List<SEDCertificate> getCertificates() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<SEDCertificate> getRootCACertificates() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }



  @Override
  public void removeCertificateFromStore(SEDCertificate crt) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void changeAlias(String oldAlias, String newAlias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void changeRootCAAlias(String oldAlias, String newAlias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void removeCertificateFromRootCAStore(SEDCertificate crt) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SEDCertificate getSEDCertificatForAlias(String alias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PrivateKey getPrivateKeyForAlias(String alias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public X509Certificate getX509CertForAlias(String alias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PrivateKey getPrivateKeyForX509Cert(X509Certificate xrc) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void changeKeystorePassword(String newPasswd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void changeRootCAPassword(String newPasswd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }



}
