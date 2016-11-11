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
package si.jrc.msh.sec;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.sec.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
public class TestProperties {

  public static final String KEYSTORE = "/certs/msh.e-box-b-keystore.jks";
  public static final String KEYSTORE_PASSWORD = "test1234";
  public static final String KEYSTORE_TYPE = "JKS";
  public static final String KEY_PASSWORD = "key1234";
  public static final String SIGN_KEY_ALIAS = "msh.e-box-b.si";
  
   public static final String KEYSTORE_A = "/certs/msh.e-box-a-keystore.jks";
  public static final String KEYSTORE_PASSWORD_A = "test1234";
  public static final String KEYSTORE_TYPE_A = "JKS";
  public static final String KEY_PASSWORD_A = "key1234";
  public static final String SIGN_KEY_ALIAS_A = "msh.e-box-a.si";

  public static X509Certificate S_CERT_B;
  public static PrivateKey S_KEY_B;
  public static X509Certificate S_CERT_A;
  public static PrivateKey S_KEY_A;

  static {
    System.getProperties().setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR,
        "src/test/resources/certs/");
    KeystoreUtils cu = new KeystoreUtils();
    try {
      
       KeyStore ksA =
          cu.getKeystore(TestProperties.class.getResourceAsStream(KEYSTORE_A), KEYSTORE_TYPE_A,
              KEYSTORE_PASSWORD_A.toCharArray());

      S_KEY_A = (PrivateKey)cu.getPrivateKeyForAlias(ksA, TestProperties.SIGN_KEY_ALIAS_A,
          TestProperties.KEY_PASSWORD_A);
      S_CERT_A = cu.getTrustedCertForAlias(ksA, TestProperties.SIGN_KEY_ALIAS_A);
      
      // set store key password parameters

      KeyStore ks =
          cu.getKeystore(TestProperties.class.getResourceAsStream(KEYSTORE), KEYSTORE_TYPE,
              KEYSTORE_PASSWORD.toCharArray());

      S_KEY_B = (PrivateKey)cu.getPrivateKeyForAlias(ks, TestProperties.SIGN_KEY_ALIAS,
          TestProperties.KEY_PASSWORD);
      S_CERT_B = cu.getTrustedCertForAlias(ks, TestProperties.SIGN_KEY_ALIAS);
    } catch (SEDSecurityException ex) {
      ex.printStackTrace();
    }
  }

}
