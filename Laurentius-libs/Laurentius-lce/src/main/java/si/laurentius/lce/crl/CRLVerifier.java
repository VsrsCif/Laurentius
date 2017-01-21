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
import java.util.Properties;

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
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;

/**
 * Class helper for handling CRLs for given X509 certificate.
 * Class was heavily inspired by Svetlin Nakov
 * http://www.nakov.com/blog/2009/12/01/x509-certificate-validation-in-java-build-and-verify-chain-and-verify-clr-with-bouncy-castle/
 * @author Jože Rihtaršič
 */
public class CRLVerifier {

  final public static String CF_X509 = "X.509";
  final public static String S_CRL_DEST_POINTS = "2.5.29.31";
  final public static String S_INITIAL_CONTEXT_FACTORY_NAME ="com.sun.jndi.ldap.LdapCtxFactory";
  final public static String S_PROTOCOL_LDAP = "ldap://";
  final public static String S_PROTOCOL_HTTP = "http://";
  final public static String S_PROTOCOL_HTTPS = "https://";
  final public static String S_PROTOCOL_FTP = "ftp://";
  
  protected static final SEDLogger LOG = new SEDLogger(CRLVerifier.class);


  /**
   * Downloads CRL from given URL. Supports http, https, ftp and ldap based URLs.
   * 
   * @param crlURL
   * @return
   * @throws IOException
   * @throws CertificateException
   * @throws CRLException
   * @throws NamingException 
   */
  public static X509CRL downloadCRL(final String crlURL)
      throws IOException,
      CertificateException, CRLException,
       NamingException {

    if (Utils.isEmptyString(crlURL)){
      throw new NullPointerException("CRL URL should not be null or empty string");
    }
    String crlprv = crlURL.trim();
    
    
    
    X509CRL crl = null;

    if (crlURL.startsWith(S_PROTOCOL_HTTP) 
        || crlURL.startsWith(S_PROTOCOL_HTTP.toUpperCase()) 
        || crlURL.startsWith(S_PROTOCOL_HTTPS) 
        || crlURL.startsWith(S_PROTOCOL_HTTPS.toUpperCase()) 
        ) {
      URL url = new URL(crlprv);
      try (InputStream crlStream = url.openStream()) {
        CertificateFactory cf = CertificateFactory.getInstance(CF_X509);
        crl = (X509CRL) cf.generateCRL(crlStream);
      }

    } else if (crlURL.startsWith(S_PROTOCOL_LDAP) 
        || crlURL.startsWith(S_PROTOCOL_LDAP.toUpperCase()) ) {

      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY,
          S_INITIAL_CONTEXT_FACTORY_NAME);
      env.put(Context.PROVIDER_URL, crlURL);

      DirContext ctx = new InitialDirContext(env);
      Attributes avals = ctx.getAttributes("");
      Attribute aval = avals.get("certificateRevocationList;binary");
      byte[] val = (byte[]) aval.get();
      if (val != null && val.length != 0) {
        InputStream inStream = new ByteArrayInputStream(val);
        CertificateFactory cf = CertificateFactory.getInstance(CF_X509);
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

              if (scrl.getHttp() == null && (
                  val.startsWith(S_PROTOCOL_HTTPS) 
                  || val.startsWith(S_PROTOCOL_HTTP)) ) {
                scrl.setHttp(derStr.getString());
              }
              else if (val.startsWith(S_PROTOCOL_LDAP) && scrl.getLdap() == null) {
                scrl.setLdap(derStr.getString());
              } else {
                LOG.formatedWarning("Unsupported crl protocol '%s'!", derStr.getString());
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
