/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons.utils.sec;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;

/**
 *
 * @author sluzba
 */
public class KeystoreUtilsTest {

  public static String KEYSTORE_FILEPATH = "src/test/resources/certs/msh.e-box-a-keystore.jks";
  public static String KEYSTORE_TYPE = "JKS";
  public static String KEYSTORE_PASSWD = "test1234";
  public static String KEYSTORE_KEY1_ALIAS = "msh.e-box-a.si";
  public static String KEYSTORE_KEY1_ISSUER = "CN=msh.e-box-a.si, OU=test, OU=msh, OU=jrc, OU=si";
  public static String KEYSTORE_KEY1_SUBJECT = "CN=msh.e-box-a.si, OU=test, OU=msh, OU=jrc, OU=si";
  public static String KEYSTORE_KEY1_SERIAL = "1725505630";
  public static String KEYSTORE_KEY1_PASS = "key1234";

  public static String TRUSTSTORE_FILEPATH = "src/test/resources/certs/msh.e-box-a-truststore.jks";
  public static String TRUSTSTORE_TYPE = "JKS";
  public static String TRUSTSTORE_PASSWD = "test1234";
  public static String TRUSTSTORE_CERT1_ALIAS = "msh.e-box-a.si";
  public static String TRUSTSTORE_CERT1_ISSUER = "CN=msh.e-box-a.si, OU=test, OU=msh, OU=jrc, OU=si";
  public static String TRUSTSTORE_CERT1_SUBJECT =
      "CN=msh.e-box-a.si, OU=test, OU=msh, OU=jrc, OU=si";
  public static String TRUSTSTORE_CERT1_SERIAL = "1725505630";

  public KeystoreUtilsTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @Before
  public void setUp() {
  }

  private SEDCertStore createKeystore() {

    SEDCertStore cs = new SEDCertStore();
    cs.setFilePath(KEYSTORE_FILEPATH);
    cs.setName("def-keystore");
    cs.setPassword(KEYSTORE_PASSWD);
    cs.setType(KEYSTORE_TYPE);

    SEDCertificate c = new SEDCertificate();
    c.setAlias(KEYSTORE_KEY1_ALIAS);
    c.setIssuerDN(KEYSTORE_KEY1_ISSUER);
    c.setSubjectDN(KEYSTORE_KEY1_SUBJECT);
    c.setSerialNumber(KEYSTORE_KEY1_SERIAL);
    c.setType("X.509");
    c.setKeyEntry(true);
    c.setKeyPassword(KEYSTORE_KEY1_PASS);
    Calendar cal = Calendar.getInstance();
    cal.set(2016, 02, 29, 14, 55, 28);
    c.setValidFrom(cal.getTime());
    cal.set(2021, 02, 27, 14, 55, 28);
    c.setValidTo(cal.getTime());
    
    cs.getSEDCertificates().add(c);
    return cs;
  }

  private SEDCertStore createTruststore() {

    SEDCertStore cs = new SEDCertStore();
    cs.setFilePath(TRUSTSTORE_FILEPATH);
    cs.setName("def-truststore");
    cs.setPassword(TRUSTSTORE_PASSWD);
    cs.setType(TRUSTSTORE_TYPE);

    SEDCertificate c = new SEDCertificate();
    c.setAlias(TRUSTSTORE_CERT1_ALIAS);
    c.setIssuerDN(TRUSTSTORE_CERT1_ISSUER);
    c.setSubjectDN(TRUSTSTORE_CERT1_SUBJECT);
    c.setSerialNumber(TRUSTSTORE_CERT1_SERIAL);
    c.setType("X.509");
    c.setKeyEntry(false);

    Calendar cal = Calendar.getInstance();
    cal.set(2016, 02, 29, 14, 55, 28);
    c.setValidFrom(cal.getTime());
    cal.set(2021, 02, 27, 14, 55, 28);
    c.setValidTo(cal.getTime());

    cs.getSEDCertificates().add(c);
    return cs;
  }

  /**
   * Test of getKeystore method, of class KeystoreUtils.
   */
  @Test
  public void testGetKeystore_SEDCertStore()
      throws Exception {
    SEDCertStore sc = createKeystore();
    KeystoreUtils instance = new KeystoreUtils();
    KeyStore result = instance.getKeystore(sc);
    assertNotNull(result);
    assertTrue(result.containsAlias(KEYSTORE_KEY1_ALIAS));

  }

  /**
   * Test of getTrustManagers method, of class KeystoreUtils.
   */
  @Test
  public void testGetTrustManagers()
      throws Exception {
    System.out.println("getTrustManagers");
    SEDCertStore sc = createTruststore();
    KeystoreUtils instance = new KeystoreUtils();

    TrustManager[] result = instance.getTrustManagers(sc);
    assertNotNull(result);
    assertEquals(1, result.length);

  }

  /**
   * Test of getKeyManagers method, of class KeystoreUtils.
   */
  @Test
  public void testGetKeyManagers()
      throws Exception {
    System.out.println("getKeyManagers");
    SEDCertStore sc = createKeystore();
    KeystoreUtils instance = new KeystoreUtils();
    KeyManager[] result = instance.getKeyManagers(sc, KEYSTORE_KEY1_ALIAS);
    assertNotNull(result);
    assertEquals(1, result.length);
  }

  /**
   * Test of getKeyManagersForAlias method, of class KeystoreUtils.
   */
  @Test
  public void testGetKeyManagersForAlias()
      throws Exception {
    System.out.println("getKeyManagersForAlias");
    SEDCertStore sc = createKeystore();
    String alias = KEYSTORE_KEY1_ALIAS;
    KeystoreUtils instance = new KeystoreUtils();
    KeyManager[] result = instance.getKeyManagersForAlias(sc, alias);
    assertNotNull(result);
    assertEquals(1, result.length);

  }

  /**
   * Test of getKeystore method, of class KeystoreUtils.
   */
  @Test
  public void testGetKeystore_3args()
      throws Exception {

    try (InputStream isTrustStore = new FileInputStream(KEYSTORE_FILEPATH)) {
      String trustStoreType = KEYSTORE_TYPE;
      char[] password = KEYSTORE_PASSWD.toCharArray();
      KeystoreUtils instance = new KeystoreUtils();
      KeyStore result = instance.getKeystore(isTrustStore, trustStoreType, password);
      assertNotNull(result);

    }
  }

  /**
   * Test of getPrivateKeyEntryForAlias method, of class KeystoreUtils.
   */
  @Test
  public void testGetPrivateKeyEntryForAlias_3args()
      throws Exception {
    String alias = KEYSTORE_KEY1_ALIAS;
    String passwd = KEYSTORE_KEY1_PASS;

    KeyStore ks = null;
    try (InputStream isTrustStore = new FileInputStream(KEYSTORE_FILEPATH)) {
      String trustStoreType = KEYSTORE_TYPE;
      char[] password = KEYSTORE_PASSWD.toCharArray();
      KeystoreUtils instance = new KeystoreUtils();
      ks = instance.getKeystore(isTrustStore, trustStoreType, password);
      assertNotNull(ks);
      KeyStore.PrivateKeyEntry result = instance.getPrivateKeyEntryForAlias(ks, alias, passwd);
      assertNotNull(result);
    }
  }

  /**
   * Test of getPrivateKeyForAlias method, of class KeystoreUtils.
   */
  @Test
  public void testGetPrivateKeyForAlias()
      throws Exception {

    String alias = KEYSTORE_KEY1_ALIAS;
    String psswd = KEYSTORE_KEY1_PASS;

    KeyStore ks;
    try (InputStream isTrustStore = new FileInputStream(KEYSTORE_FILEPATH)) {
      String trustStoreType = KEYSTORE_TYPE;
      char[] password = KEYSTORE_PASSWD.toCharArray();
      KeystoreUtils instance = new KeystoreUtils();
      ks = instance.getKeystore(isTrustStore, trustStoreType, password);
      assertNotNull(ks);
      Key result = instance.getPrivateKeyForAlias(ks, alias, psswd);
      assertNotNull(result);
    }
  }

  

  }
