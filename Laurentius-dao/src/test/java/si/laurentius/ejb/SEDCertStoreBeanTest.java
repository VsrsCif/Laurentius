/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import si.laurentius.ejb.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.Utils;
import si.laurentius.ejb.db.MockUserTransaction;
import si.laurentius.lce.KeystoreUtils;

/**
 *
 * @author sluzba
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SEDCertStoreBeanTest extends TestUtils {

  static EntityManagerFactory memfMSHFactory = null;
  static final SEDCertStoreBean mTestInstance = new SEDCertStoreBean();
  KeystoreUtils mku = new KeystoreUtils();
  static final String[] CERT_SAMPLES = new String[]{"test-digicert.crt",
    "test-entrust.crt", "test-symantec.crt", "test-sigovca-web.cer"};

  public SEDCertStoreBeanTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {

    // ---------------------------------
    // set logger
    setLogger(SEDCertStoreBeanTest.class.getSimpleName());

    // create persistence unit
    memfMSHFactory = Persistence.createEntityManagerFactory(
            PERSISTENCE_UNIT_NAME);
    mTestInstance.memEManager = memfMSHFactory.createEntityManager();
    mTestInstance.mutUTransaction
            = new MockUserTransaction(mTestInstance.memEManager.getTransaction());

  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Test
  public void test_A_createCertStores() throws Exception {
    System.out.println("test_A_createCertStores");

    File cs = SEDSystemProperties.getCertstoreFile();
    if (cs.exists()) {
      cs.delete();
    }
    File csRootCA = SEDSystemProperties.getRootCAStoreFile();
    if (csRootCA.exists()) {
      csRootCA.delete();
    }

    assertFalse("File certstore not exists", cs.exists());
    // get certsore  creates new keystore in not exists  
    KeyStore ks = mTestInstance.getCertStore();
    assertNotNull("Keystore must not be null", ks);
    assertTrue("File '" + cs.getAbsolutePath() + "' exists", cs.exists());
    // test rootCA store
    assertFalse("File csRootCA not exists", csRootCA.exists());
    ks = mTestInstance.getRootCAStore();
    assertNotNull("csRootCA must not be null", ks);
    assertTrue("File csRootCA exists", cs.exists());

  }
  
  @Test
  public void test_B_CertStoresInvalidPasswd() throws Exception {
    System.out.println("test_A_CertStoresInvalidPasswd");

    File cs = SEDSystemProperties.getCertstoreFile();
    if (cs.exists()) {
      cs.delete();
    }
    assertFalse("File certstore not exists", cs.exists());
    // get certsore  creates new keystore in not exists  
    KeyStore ks = mTestInstance.getCertStore();
    assertNotNull("Keystore must not be null", ks);
    assertTrue("File '" + cs.getAbsolutePath() + "'not exists", cs.exists());
    // test rootCA store
    mTestInstance.mscCacheList.clearCachedList(SEDCertPassword.class);;

     ks = mTestInstance.getCertStore();
     assertNotNull("Keystore must not be null", ks);
  }

  @Test
  public void test_B_addCertToCertStores() throws Exception {
    System.out.println("test_B_addCertToCertStores");

    String alias = Utils.getUUID("als");
    X509Certificate xc = mku.getCertFromInputStream(
            SEDCertStoreBeanTest.class.getResourceAsStream(
                    "/certs/sample/sigov-ca.crt"));
    assertNotNull("Test X509Certificate must not be null", xc);

    // get certstore  

    assertNotNull("Keystore must not be null", mTestInstance.getCertStore());
    int iSize = mTestInstance.getCertStore().size();
    mTestInstance.addCertToCertStore(xc, alias);
    // cert added to keystore
    assertEquals("Cert is added to keystore", iSize + 1, mTestInstance.getCertStore().size());
    assertEquals("Certificate list is updated", iSize + 1, mTestInstance.
            getCertificates().size());

    X509Certificate xcNew = mTestInstance.getX509CertForAlias(alias);

    assertNotNull(xcNew);
    assertEquals(xc, xcNew);

  }

  @Test
  public void test_C_addCertToRootCAStores() throws Exception {
    System.out.println("test_C_addCertToRootCAStores");

    String alias = Utils.getUUID("als");
    X509Certificate xc = mku.getCertFromInputStream(
            SEDCertStoreBeanTest.class.getResourceAsStream(
                    "/certs/sample/sigov-ca.crt"));
    assertNotNull("Test X509Certificate must not be null", xc);

    // get certstore  
    KeyStore ks = mTestInstance.getRootCAStore();
    assertNotNull("Keystore must not be null", ks);
    int iSize = ks.size();
    mTestInstance.addCertToRootCA(xc, alias);
    // cert added to keystore
    assertEquals("Cert is added to keystore", iSize + 1, mTestInstance.getCertStore().size());

    List<X509Certificate> rootCACerts = mTestInstance.getRootCA509Certs();
    assertEquals("Certificate list is updated", iSize + 1, rootCACerts.size());

    X509Certificate xcNew = null;
    for (X509Certificate c : rootCACerts) {
      if (c.equals(xc)) {
        xcNew = c;
        break;
      }
    }

    assertNotNull(xcNew);
  }

  @Test
  public void test_D_addKeyToToCertStore() throws Exception {
    System.out.println("test_D_addKeyToToCertStore");
    String srcAlias = "msh.e-box-a.si";
    String keyPasswd = "key1234";
    String srcStorePasswd = "test1234";

    String alias = Utils.getUUID("als");
    KeyStore ksSrc = mku.getKeystore(SEDCertStoreBeanTest.class.
            getResourceAsStream(
                    "/certs/msh.e-box-a-keystore.jks"), "JKS", srcStorePasswd.
                    toCharArray());

// get test private key
    Key key = ksSrc.getKey(srcAlias, keyPasswd.toCharArray());
    Certificate[] crt = ksSrc.getCertificateChain(srcAlias);

    // get certstore  
    KeyStore ks = mTestInstance.getCertStore();
    assertNotNull("Keystore must not be null", ks);
    int iSize = ks.size();

    mTestInstance.addKeyToToCertStore(alias, key, crt, keyPasswd);
    // cert added to keystore
    assertEquals("Cert is added to keystore", iSize + 1, mTestInstance.getCertStore().size());

    List<SEDCertificate> crts = mTestInstance.getCertificates();
    assertEquals("Certificate list is updated", iSize + 1, crts.size());

  }

  @Test
  public void test_E_removeCertificateFromStore() throws Exception {
    System.out.println("test_D_addKeyToToCertStore");
    String srcAlias = "msh.e-box-a.si";
    String keyPasswd = "key1234";
    String srcStorePasswd = "test1234";

    // add new cert
    String alias = Utils.getUUID("als");
    KeyStore ksSrc = mku.getKeystore(SEDCertStoreBeanTest.class.
            getResourceAsStream(
                    "/certs/msh.e-box-a-keystore.jks"), "JKS", srcStorePasswd.
                    toCharArray());

// get test private key
    Key key = ksSrc.getKey(srcAlias, keyPasswd.toCharArray());
    Certificate[] crt = ksSrc.getCertificateChain(srcAlias);

    // get certstore  
    KeyStore ks = mTestInstance.getCertStore();
    assertNotNull("Keystore must not be null", ks);
    int iSize = ks.size();

    mTestInstance.addKeyToToCertStore(alias, key, crt, keyPasswd);
    // cert added to keystore
    assertEquals("Cert is added to keystore", iSize + 1, mTestInstance.getCertStore().size());

    SEDCertificate scNew = mTestInstance.getSEDCertificatForAlias(alias);
    assertNotNull(scNew);

    // remove certificte 
    mTestInstance.removeCertificateFromStore(scNew);
    assertEquals("Cert is removed from keystore", iSize, mTestInstance.getCertStore().size());

    scNew = mTestInstance.getSEDCertificatForAlias(alias);
    assertNull(scNew);

  }

  @Test
  public void test_F_isCertificateRevoked() throws Exception {
    /*
    for (String certName : CERT_SAMPLES) {
      X509Certificate xc = mku.getCertFromInputStream(
              SEDCertStoreBeanTest.class.getResourceAsStream(
                      "/certs/sample/" + certName));
      Boolean bval = mTestInstance.isCertificateRevoked(xc);

      assertNotNull(certName, bval);
      assertTrue(!bval);
    }*/

  }
}
