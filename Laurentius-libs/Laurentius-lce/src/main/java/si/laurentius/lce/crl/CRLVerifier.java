/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.crl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import si.laurentius.cert.crl.SEDCertCRL;

/**
 * Class that verifies CRLs for given X509 certificate. Extracts the CRL distribution points from
 * the certificate (if available) and checks the certificate revocation status against the CRLs
 * coming from the distribution points. Supports HTTP, HTTPS, FTP and LDAP based URLs.
 *
 * @author Svetlin Nakov
 */
public class CRLVerifier {

  public static final String CF_X509 = "X.509";
  final public static String S_CRL_DEST_POINTS = "2.5.29.31";


  /**
   * Downloads CRL from given URL. Supports http, https, ftp and ldap based URLs.
   */
  public static X509CRL downloadCRL(String crlURL)
      throws IOException,
      CertificateException, CRLException,
       NamingException {

    X509CRL crl = null;

    if (crlURL.startsWith("http://") || crlURL.startsWith("https://") ||
        crlURL.startsWith("ftp://")) {
      URL url = new URL(crlURL);
      try (InputStream crlStream = url.openStream()) {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        crl = (X509CRL) cf.generateCRL(crlStream);
      }

    } else if (crlURL.startsWith("ldap://")) {

      Hashtable<String, String> env = new Hashtable<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY,
          "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, crlURL);

      DirContext ctx = new InitialDirContext(env);
      Attributes avals = ctx.getAttributes("");
      Attribute aval = avals.get("certificateRevocationList;binary");
      byte[] val = (byte[]) aval.get();
      if (val != null && val.length != 0) {
        InputStream inStream = new ByteArrayInputStream(val);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        crl = (X509CRL) cf.generateCRL(inStream);
      }
    }
    return crl;
  }

  /**
   * Extracts fCRL distribution point URLs from the "CRL Distribution Point" extension in a X.509
   * certificate.
   *
   * @param cert
   * @return
   * @throws java.security.cert.CertificateParsingException
   * @throws java.io.IOException
   */
  public static SEDCertCRL getCrlData(
      X509Certificate cert)
      throws CertificateParsingException, IOException {

    SEDCertCRL scrl = new SEDCertCRL();
    scrl.setIssuerDN(cert.getIssuerX500Principal().getName());
    byte[] dtExt = cert.getExtensionValue(S_CRL_DEST_POINTS);
    if (dtExt != null && dtExt.length != 0) {
      ASN1Primitive extensionValue = JcaX509ExtensionUtils.parseExtensionValue(dtExt);
      CRLDistPoint distPoint = CRLDistPoint.getInstance(extensionValue);
      for (DistributionPoint dp : distPoint.getDistributionPoints()) {

        DistributionPointName dpn = dp.getDistributionPoint();
        if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {

          GeneralNames generalNames = (GeneralNames) dpn.getName();
          GeneralName[] names = generalNames.getNames();
          for (GeneralName name : names) {
            if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
              DERIA5String derStr =
                  DERIA5String.getInstance((ASN1TaggedObject) name.toASN1Primitive(), false);
              String val = derStr.getString().toLowerCase();

              if (val.startsWith("http://") && scrl.getHttp() == null) {
                scrl.setHttp(derStr.getString());
              } else if (val.startsWith("ldap://") && scrl.getLdap() == null) {
                scrl.setLdap(derStr.getString());
              } else {
                // "warning unknown protocol"

              }
            }

          }

        } else {
          System.out.println("missing EXTENSION CRL LIST");
        }
      }
    }
   
    return scrl;
  }

}
