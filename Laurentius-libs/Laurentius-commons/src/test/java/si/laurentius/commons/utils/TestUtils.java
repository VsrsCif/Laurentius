/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.sec.KeystoreUtils;

/**
 *
 * @author sluzba
 */
public class TestUtils {

  private static final String KEYSTORE = "/certs/msh.e-box-b-keystore.jks";
  private static final String KEYSTORE_PASSWORD = "test1234";
  private static final String KEYSTORE_TYPE = "JKS";
  private static final String KEY_PASSWORD = "key1234";

  private static final String SIGN_KEY_ALIAS = "msh.e-box-b.si";

  public TestUtils() {
  }

  protected File createFile(String data)
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin");

    try (final FileOutputStream fos = new FileOutputStream(f)) {
      if (data != null) {
        fos.write(data.getBytes("UTF-8"));
      } else {
        fos.write("Test data".getBytes("UTF-8"));
      }
    }

    return f;
  }

  protected File createFile(File parent, String content)
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin", parent);

    try (final FileOutputStream fos = new FileOutputStream(f)) {
      if (content != null) {
        fos.write(content.getBytes("UTF-8"));
      } else {
        fos.write("Test data".getBytes("UTF-8"));
      }
    }

    return f;
  }

  protected File createEmptyFile()
      throws IOException {
    File f = File.createTempFile("hu-test", ".bin");
    return f;
  }

  public byte[] getTestByteArray()
      throws UnsupportedEncodingException {
    return ("testbuffer" + Calendar.getInstance().getTimeInMillis()).getBytes("utf-8");
  }

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
            
      throws SEDSecurityException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
    KeystoreUtils cu = new KeystoreUtils();
    KeyStore ks =
        cu.getKeystore(TestUtils.class.getResourceAsStream(KEYSTORE), KEYSTORE_TYPE,
            KEYSTORE_PASSWORD.toCharArray());
     return  cu.getPrivateKeyEntryForAlias(ks, SIGN_KEY_ALIAS, KEY_PASSWORD);

  }

}
