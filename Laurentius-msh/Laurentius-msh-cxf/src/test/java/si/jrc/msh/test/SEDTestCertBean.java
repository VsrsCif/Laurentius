/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import si.jrc.msh.interceptor.EBMSInInterceptorTest;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertUtilsInterface;
import si.laurentius.msh.pmode.X509;

/**
 *
 * @author Jože Rihtaršič
 */
public class SEDTestCertBean implements SEDCertUtilsInterface {


  KeystoreUtils mku = new KeystoreUtils();
  public static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
      EBMSInInterceptorTest.class);

  public SEDTestCertBean() {

  }

  @Override
  public Map<String, Object> createCXFDecryptionConfiguration(
          X509.Encryption enc, String decAlias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Map<String, Object> createCXFEncryptionConfiguration(
          X509.Encryption enc, String alias) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Map<String, Object> createCXFSignatureConfiguration(X509.Signature sig,
          String sigAlias) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Map<String, Object> createCXFSignatureValidationConfiguration(
          X509.Signature sig, String sigAliasProp) {
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
  public X509TrustManager getTrustManagerForAlias(String alias,
          boolean validateRootCA) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }


}
