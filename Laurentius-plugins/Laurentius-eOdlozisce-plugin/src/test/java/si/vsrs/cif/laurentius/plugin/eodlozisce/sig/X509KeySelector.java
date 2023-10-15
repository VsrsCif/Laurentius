package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class X509KeySelector extends KeySelector {

    @Override
    public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
        Iterator<?> ki = keyInfo.getContent().iterator();

        while (ki.hasNext()) {
            XMLStructure info = (XMLStructure) ki.next();
            if (!(info instanceof X509Data))
                continue;

            X509Data x509Data = (X509Data) info;
            Iterator<?> xi = x509Data.getContent().iterator();

            while (xi.hasNext()) {
                Object o = xi.next();
                if (!(o instanceof X509Certificate))
                    continue;

                final X509Certificate cert = (X509Certificate) o;

                // Check whether the certificate is valid for the given purpose
                if (purpose != null) {
                    if (purpose == KeySelector.Purpose.VERIFY) {
                        try {
                            cert.checkValidity();
                        } catch (Exception e) {
                            throw new KeySelectorException("Certificate is not valid: " + e.getMessage(), e);
                        }
                    }
                }

                return new KeySelectorResult() {
                    public Key getKey() {
                        return cert.getPublicKey();
                    }
                };
            }
        }

        throw new KeySelectorException("No X.509 certificate found for the given purpose.");
    }
}
