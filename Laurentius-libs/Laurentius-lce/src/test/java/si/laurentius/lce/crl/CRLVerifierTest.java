/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.crl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author sluzba
 */
public class CRLVerifierTest {

  public static SEDLogger LOG = new SEDLogger(CRLVerifierTest.class);

  final public static String S_CRL_DEST_POINTS = "2.5.29.31";
  final static String[][] LST_DATA_CRT = new String[][]{


    {"symantec.crt",
      "CN=Symantec Class 3 EV SSL CA - G3,OU=Symantec Trust Network,O=Symantec Corporation,C=US",
      "http://sr.symcb.com/sr.crl",
      null},
    {"entrust.crt",
      "CN=Entrust Certification Authority - L1M,OU=(c) 2014 Entrust\\, Inc. - for authorized use only,OU=See www.entrust.net/legal-terms,O=Entrust\\, Inc.,C=US",
      "http://crl.entrust.net/level1m.crl",
      null},
    {"digicert.crt",
      "CN=DigiCert SHA2 Extended Validation Server CA,OU=www.digicert.com,O=DigiCert Inc,C=US",
      "http://crl3.digicert.com/sha2-ev-server-g1.crl",
      null},};

  @BeforeClass
  public static void setUpClass() {
  }

  public CRLVerifierTest() {
  }

  @Before
  public void setUp() {
  }

  /**
   * Test of getCrlDistributionPoints method, of class CRLVerifier.
   */
  @Test
  public void test_A_GetCrlData()
          throws Exception {

    for (String[] tst : LST_DATA_CRT) {
      X509Certificate cert = null;
      try (InputStream inStrm = CRLVerifierTest.class.getResourceAsStream(
              "/certs/sample/" + tst[0])) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        cert = (X509Certificate) cf.generateCertificate(inStrm);
        SEDCertCRL crl = CRLVerifier.getCrlData(cert);
        assertCrlData(cert.getSubjectX500Principal().getName(), crl, tst[1],
                tst[2], tst[3]);
      }
    }
  }

  /**
   * Test of downloadCRL method, of class CRLVerifier.
   */
  @Test
  public void test_B_DownloadCRL_HTTP()
          throws Exception {
    Assume.assumeTrue(networkConnected());

    String crlURL = "http://www.sigov-ca.gov.si/crl/sigov-ca.crl";
    X509CRL expResult = null;
    X509CRL result = CRLVerifier.downloadCRL(crlURL);
    Assert.assertNotNull(result);

    Date dt = Calendar.getInstance().getTime();
    Assert.assertTrue(dt.before(result.getNextUpdate()));
    Assert.assertTrue(dt.after(result.getThisUpdate()));

  }

  /**
   * Test of downloadCRL method, of class CRLVerifier.
   */
  @Test
  public void test_B_DownloadCRL_LDAP()
          throws Exception {
    Assume.assumeTrue(networkConnected());

    System.out.println("downloadCRL_LDAP");
    String crlURL = "ldap://x500.gov.si/cn=SIGEN-CA%20G2,organizationIdentifier=VATSI-17659957,o=Republika%20Slovenija,c=SI?certificateRevocationList";

    X509CRL result = CRLVerifier.downloadCRL(crlURL);
    Assert.assertNotNull(result);

    Date dt = Calendar.getInstance().getTime();
    Assert.assertTrue(dt.before(result.getNextUpdate()));
    Assert.assertTrue(dt.after(result.getThisUpdate()));

  }

  private void assertCrlData(String message, SEDCertCRL crl, String issuer,
          String http,
          String ldap)
          throws CertificateParsingException, IOException {

    Assert.assertNotNull(message, crl);
    Assert.assertEquals(message, issuer, crl.getIssuerDN());
    Assert.assertTrue(message, crl.getUrls().size() > 0);
    if (http != null) {
      assertContainsUrl(message, crl.getUrls(), http);
    }
    if (ldap != null) {
      assertContainsUrl(message, crl.getUrls(), ldap);
    }
  }

  private void assertContainsUrl(String message, List<SEDCertCRL.Url> lst,
          String url) {
    boolean contains = false;
    for (SEDCertCRL.Url u : lst) {
      if (u.getValue().equalsIgnoreCase(url)) {
        contains = true;
        break;
      };
    }
    Assert.assertTrue(message, contains);
  }

  boolean networkConnected() {
    boolean bSuc = false;
    String testAddress = "www.google.com";
    try {

      int timeout = 2000;
      InetAddress[] addresses = InetAddress.getAllByName(testAddress);
      for (InetAddress address : addresses) {
        if (address.isReachable(timeout)) {
          bSuc = true;
          break;
        }
      }
      
    } catch (UnknownHostException ex) {
      LOG.logError("Unknown host " + testAddress, ex);
    } catch (IOException ex) {
      LOG.logError("IOException from host " + testAddress, ex);
    }
    return bSuc;
  }
}
