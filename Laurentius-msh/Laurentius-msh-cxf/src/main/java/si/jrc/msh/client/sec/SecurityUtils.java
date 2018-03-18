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
package si.jrc.msh.client.sec;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageChecker;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageUtil;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.X509;
import si.jrc.msh.interceptor.EBMSOutInterceptor;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.XPath;


/**
 *
 * @author Jože Rihtaršič
 */
public class SecurityUtils {

  protected final static SEDLogger LOG = new SEDLogger(EBMSOutInterceptor.class);

  
  public static final CryptoCoverageChecker configureCryptoCoverageCheckerInterceptors(Security sc) {
    CryptoCoverageChecker cc;

    if (sc == null || sc.getX509() == null) {
      return null;
    }
    Map<String, String> prefixes = new HashMap<>();
    List<CryptoCoverageChecker.XPathExpression> xpaths = new ArrayList<>();

    if (sc.getX509().getSignature() != null &&
         sc.getX509().getSignature().getReference() != null &&
         sc.getX509().getSignature().getReference().getElements() != null) {
      X509.Signature snc = sc.getX509().getSignature();
      X509.Signature.Reference rsgn = snc.getReference();
      

      for (XPath el : rsgn.getElements().getXPaths()) {
        for (XPath.Namespace ns : el.getNamespaces()) {
          prefixes.put(ns.getPrefix(), ns.getNamespace());
        }
        xpaths.add(new CryptoCoverageChecker.XPathExpression(el.getXpath(),
            CryptoCoverageUtil.CoverageType.SIGNED, CryptoCoverageUtil.CoverageScope.ELEMENT));
      }
    }

    if (sc.getX509().getEncryption() != null &&
         sc.getX509().getEncryption().getReference() != null &&
         sc.getX509().getEncryption().getReference().getElements() != null) {
      X509.Encryption snc = sc.getX509().getEncryption();
      X509.Encryption.Reference rsgn = snc.getReference();

      for (XPath el : rsgn.getElements().getXPaths()) {
        for (XPath.Namespace ns : el.getNamespaces()) {
          prefixes.put(ns.getPrefix(), ns.getNamespace());
        }
        xpaths.add(new CryptoCoverageChecker.XPathExpression(el.getXpath(),
            CryptoCoverageUtil.CoverageType.ENCRYPTED, CryptoCoverageUtil.CoverageScope.ELEMENT));
      }
    }

    cc = new CryptoCoverageChecker(prefixes, xpaths);
    
    return cc;
  }
  
  


}
