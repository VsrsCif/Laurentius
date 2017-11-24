/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.interfaces;

import java.util.Map;
import java.util.Properties;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.msh.pmode.X509;

/**
 *
 * @author sluzba
 */
public interface SEDCertUtilsInterface {

  Properties getCXFKeystoreProperties(String alias) throws SEDSecurityException;

  Properties getCXFTruststoreProperties(String alias) throws SEDSecurityException;
  Map<String, Object> createCXFEncryptionConfiguration(X509.Encryption enc,
           String alias) throws SEDSecurityException;
  Map<String, Object> createCXFSignatureValidationConfiguration(
          X509.Signature sig,
          String sigAliasProp) throws SEDSecurityException;
  Map<String, Object> createCXFSignatureConfiguration(X509.Signature sig,
         String sigAlias) throws SEDSecurityException;
  Map<String, Object> createCXFDecryptionConfiguration(
          X509.Encryption enc,
          String decAlias)  throws SEDSecurityException;

  X509KeyManager[] getKeyManagerForAlias(String alias) throws SEDSecurityException;

  

  X509TrustManager getTrustManagerForAlias(String alias, boolean validateRootCA) throws SEDSecurityException;
  
}
