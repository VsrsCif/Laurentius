package si.laurentius.lce.utils;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.lce.KeystoreUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jože Rihtaršič
 */
public class TestUtils {

  private static final String KEYSTORE = "/certs/msh.e-box-b-keystore.jks";
  private static final String KEYSTORE_PASSWORD = "test1234";
  private static final String KEYSTORE_TYPE = "JKS";
  private static final String KEY_PASSWORD = "key1234";

  private static final String SIGN_KEY_ALIAS = "msh.e-box-b.si";

  public X509Certificate getTestCertificate()
      throws SEDSecurityException {
    KeystoreUtils cu = new KeystoreUtils();
    KeyStore ks =
        cu.getKeystore(TestUtils.class.getResourceAsStream(KEYSTORE), KEYSTORE_TYPE,
            KEYSTORE_PASSWORD.toCharArray());

    // sign key cert
    return cu.getTrustedCertForAlias(ks, SIGN_KEY_ALIAS);
  }

  public KeyStore.PrivateKeyEntry getTestPrivateKey()

      throws SEDSecurityException, KeyStoreException, NoSuchAlgorithmException,
      UnrecoverableKeyException {
    KeystoreUtils cu = new KeystoreUtils();
    KeyStore ks =
        cu.getKeystore(TestUtils.class.getResourceAsStream(KEYSTORE), KEYSTORE_TYPE,
            KEYSTORE_PASSWORD.toCharArray());
    return cu.getPrivateKeyEntryForAlias(ks, SIGN_KEY_ALIAS, KEY_PASSWORD);

  }
}
