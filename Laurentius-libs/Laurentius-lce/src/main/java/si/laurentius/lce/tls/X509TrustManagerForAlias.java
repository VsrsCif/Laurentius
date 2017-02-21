/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.tls;

import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.X509TrustManager;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.lce.KeystoreUtils;

/**
 *
 * @author sluzba
 */
public class X509TrustManagerForAlias implements X509TrustManager {

  protected final static SEDLogger LOG = new SEDLogger(KeystoreUtils.class);
  
  private final X509Certificate msTrustedCert;
  private final List<X509Certificate> mlstAcceptedIssuers;
  
  private static final X509Certificate[] DUMMY_ISSUER_LIST = new X509Certificate[]{};

  public X509TrustManagerForAlias( X509Certificate crt, List<X509Certificate> aisLst) {
    this.msTrustedCert = crt;
    this.mlstAcceptedIssuers = aisLst;

  }

  @Override
  public void checkClientTrusted(X509Certificate[] xcs, String string)
      throws CertificateException {
    //mkmKeyManager.checkClientTrusted(xcs, string);
     StringWriter sw = new StringWriter();
    if (msTrustedCert != null) {
      for (X509Certificate c : xcs) {
        if (msTrustedCert.equals(c)) {
          return;
        }
        sw.append(c.getSubjectX500Principal().getName());
        sw.append(",");
      }
       throw new CertificateException("Bad client certificate: " + sw.toString() + " expected: " +
        msTrustedCert.getSubjectX500Principal().getName());
    }
    throw new CertificateException("Bad client certificate: " + sw.toString() + " none certificate is expected!");

  }

  @Override
  public void checkServerTrusted(X509Certificate[] xcs, String string)
      throws CertificateException {
    StringWriter sw = new StringWriter();
    if (msTrustedCert != null) {
      for (X509Certificate c : xcs) {
        if (msTrustedCert.equals(c)) {
          return;
        }
        sw.append(c.getSubjectX500Principal().getName());
        sw.append(",");
      }
       throw new CertificateException("Bad server certificate: " + sw.toString() + " expected: " +
        msTrustedCert.getSubjectX500Principal().getName());
    }
    throw new CertificateException("Bad server certificate: " + sw.toString() + " none certificate is expected!");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {    
    
    return mlstAcceptedIssuers!= null?mlstAcceptedIssuers.toArray(new X509Certificate[0]):DUMMY_ISSUER_LIST;
  }

}
