package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import org.apache.pdfbox.examples.signature.TSAClient;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificate;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAttributeTableGenerationException;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

public class PadesSignatureContainerBc implements SignatureInterface {

    public PadesSignatureContainerBc(X509CertificateHolder x509CertificateHolder, ContentSigner contentSigner, TSAClient tsaClient) throws OperatorCreationException {
        this.contentSigner = contentSigner;
        this.tsaClient = tsaClient;
        this.x509CertificateHolder = x509CertificateHolder;

        digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            CMSTypedData msg = new CMSTypedDataInputStream(content);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            gen.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider)
                            .setSignedAttributeGenerator(new PadesSignedAttributeGenerator())
                            .setUnsignedAttributeGenerator(new PadesUnsignedAttributeGenerator())
                            .build(contentSigner, x509CertificateHolder));

            gen.addCertificates(new JcaCertStore(Collections.singleton(x509CertificateHolder)));

            CMSSignedData sigData = gen.generate(msg, false);
            return sigData.getEncoded();
        } catch (OperatorCreationException | GeneralSecurityException | CMSException e) {
            throw new IOException(e);
        }
    }

    final ContentSigner contentSigner;
    final X509CertificateHolder x509CertificateHolder;
    final TSAClient tsaClient;

    final DigestCalculatorProvider digestCalculatorProvider;

    class CMSTypedDataInputStream implements CMSTypedData {
        InputStream in;

        public CMSTypedDataInputStream(InputStream is) {
            in = is;
        }

        @Override
        public ASN1ObjectIdentifier getContentType() {
            return PKCSObjectIdentifiers.data;
        }

        @Override
        public Object getContent() {
            return in;
        }

        @Override
        public void write(OutputStream out) throws IOException,
                CMSException {
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
        }
    }

    class PadesSignedAttributeGenerator implements CMSAttributeTableGenerator {
        @Override
        public AttributeTable getAttributes(@SuppressWarnings("rawtypes") Map params) throws CMSAttributeTableGenerationException {
            String currentAttribute = null;
            try {
                ASN1EncodableVector signedAttributes = new ASN1EncodableVector();
                currentAttribute = "SigningCertificateAttribute";
                AlgorithmIdentifier digAlgId = (AlgorithmIdentifier) params.get(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER);
                signedAttributes.add(createSigningCertificateAttribute(digAlgId));
                currentAttribute = "ContentType";
                ASN1ObjectIdentifier contentType = ASN1ObjectIdentifier.getInstance(params.get(CMSAttributeTableGenerator.CONTENT_TYPE));
                signedAttributes.add(new Attribute(CMSAttributes.contentType, new DERSet(contentType)));
                currentAttribute = "MessageDigest";
                byte[] messageDigest = (byte[])params.get(CMSAttributeTableGenerator.DIGEST);
                signedAttributes.add(new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString(messageDigest))));

                return new AttributeTable(signedAttributes);
            } catch (Exception e) {
                throw new CMSAttributeTableGenerationException(currentAttribute, e);
            }
        }

        Attribute createSigningCertificateAttribute(AlgorithmIdentifier digAlg) throws IOException, OperatorCreationException {
            final IssuerSerial issuerSerial = getIssuerSerial();
            DigestCalculator digestCalculator = digestCalculatorProvider.get(digAlg);
            digestCalculator.getOutputStream().write(x509CertificateHolder.getEncoded());
            final byte[] certHash = digestCalculator.getDigest();

            if (OIWObjectIdentifiers.idSHA1.equals(digAlg.getAlgorithm())) {
                final ESSCertID essCertID = new ESSCertID(certHash, issuerSerial);
                SigningCertificate signingCertificate = new SigningCertificate(essCertID);
                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("1.2.840.113549");
                return new Attribute(oid, new DERSet(signingCertificate));
            } else {
                ESSCertIDv2 essCertIdv2;
                if (NISTObjectIdentifiers.id_sha256.equals(digAlg.getAlgorithm())) {
                    // SHA-256 is default
                    essCertIdv2 = new ESSCertIDv2(null, certHash, issuerSerial);
                } else {
                    essCertIdv2 = new ESSCertIDv2(digAlg, certHash, issuerSerial);
                }
                SigningCertificateV2 signingCertificateV2 = new SigningCertificateV2(essCertIdv2);
                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("1.2.840.113549");
                return new Attribute(oid, new DERSet(signingCertificateV2));
            }
        }

        public IssuerSerial getIssuerSerial() {
            final X500Name issuerX500Name = x509CertificateHolder.getIssuer();
            final GeneralName generalName = new GeneralName(issuerX500Name);
            final GeneralNames generalNames = new GeneralNames(generalName);
            final BigInteger serialNumber = x509CertificateHolder.getSerialNumber();
            return new IssuerSerial(generalNames, serialNumber);
        }
    }

    class PadesUnsignedAttributeGenerator implements CMSAttributeTableGenerator {
        @Override
        public AttributeTable getAttributes(@SuppressWarnings("rawtypes") Map params) throws CMSAttributeTableGenerationException {
            if (tsaClient == null)
                return null;
            try {
                ASN1EncodableVector unsignedAttributes = new ASN1EncodableVector();
                byte[] signature = (byte[])params.get(CMSAttributeTableGenerator.SIGNATURE);
                byte[] timestamp = tsaClient.getTimeStampToken(signature).getEncoded();
                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier("1.2.840.113549");
                unsignedAttributes.add(new Attribute(oid, new DERSet(ASN1Primitive.fromByteArray(timestamp))));
                return new AttributeTable(unsignedAttributes);
            } catch (Exception e) {
                throw new CMSAttributeTableGenerationException("", e);
            }
        }
    }
}