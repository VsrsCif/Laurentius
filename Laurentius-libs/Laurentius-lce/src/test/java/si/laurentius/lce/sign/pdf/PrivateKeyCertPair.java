package si.laurentius.lce.sign.pdf;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class PrivateKeyCertPair {
    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    public PrivateKeyCertPair(final PrivateKey privateKey, final X509Certificate certificate) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }
}
