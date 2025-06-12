/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce.sign.pdf;

import static java.nio.file.Files.deleteIfExists;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import si.laurentius.lce.TestProperties;

/**
 * @author sluzba
 */
public class SignUtilsTest {

    public static final String S_TEMPLATE_IMAGE = "/laurentius_pdf_template.png";
    public static final String S_PDF_GRID_TEST = "gridtest.pdf";
    protected static final String PDF_SIGN_HOME = "target/";
    protected static final String PDF_SIGN_VISUALIZATION = "signed_visualization.pdf";
    protected static final String PDF_SIGN_VISUALIZATION_DEF = "signed_def_visualization.pdf";
    protected static final String PDF_SIGN_SERVER = "double_signed.pdf";

    @BeforeClass
    public static void setUpClass()
            throws IOException {

        try (InputStream pdfStream = SignUtilsTest.class.getResourceAsStream("/" + S_PDF_GRID_TEST);
             FileOutputStream fos = new FileOutputStream(PDF_SIGN_HOME + S_PDF_GRID_TEST)) {
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = pdfStream.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }

        }

    }

    @Before
    public void setUp() {
    }

    /**
     * /**
     * Test of signPDF method, of class SignUtils.
     */
    @Test
    public void test_A_SignPDF()
            throws Exception {

        PrivateKey privateKeyB = TestProperties.S_KEY_B;
        assertNotNull("Missing private key", privateKeyB);
        X509Certificate certB = TestProperties.S_CERT_B;
        assertNotNull("Missing private key Certificate", certB);

        SignUtils signPdf = new SignUtils(privateKeyB, certB);

        File signedDocumentFile;
        File documentFile = new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
        int page;
        try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {

            assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);
            signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
            // page is 1-based here
            page = 1;
            signPdf.setVisibleSignDesigner(documentFile, 10,
                    10,
                    -50, imageStream, page);
            signPdf.setVisibleSignatureProperties("name", "location", "Security", 0, page, true);

            signPdf.signPDF(documentFile, signedDocumentFile, false, "Slovenia", "ZPP Delivery");

        }
    }

    /**
     * Test of test_B_DoubleSignPDF method, of class SignUtils.
     */
    @Test
    public void test_B_DoubleSignPDF()
            throws Exception {

        // "server" signature
        PrivateKey privateKeyA = TestProperties.S_KEY_A;
        assertNotNull("Missing private key", privateKeyA);
        X509Certificate certA = TestProperties.S_CERT_A;
        assertNotNull("Missing private key Certificate", certA);

        File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
        assertTrue("Missing signed document: '" + PDF_SIGN_VISUALIZATION + "'!",
                signedDocumentFile.exists());

        // "server" signature
        SignUtils signPdfServer = new SignUtils(privateKeyA, certA);
        File dblSignedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_SERVER);
        signPdfServer.signPDF(signedDocumentFile, dblSignedDocumentFile, false, "Slovenia", "ZPP Delivery");


    }

    /**
     * Test of test_B_DoubleSignPDF method, of class SignUtils.
     */
    @Test
    public void test_C_getSignerCertificate()
            throws Exception {
        File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
        ValidateSignatureUtils vsu = new ValidateSignatureUtils();
        List<X509Certificate> lst = vsu.getSignatureCerts(signedDocumentFile.getAbsolutePath());

        assertEquals(1, lst.size());
        assertEquals(TestProperties.S_CERT_B, lst.get(0));

    }

    @Test
    public void test_D_getTwoSignerCertificates()
            throws Exception {
        File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_SERVER);

        ValidateSignatureUtils vsu = new ValidateSignatureUtils();
        List<X509Certificate> lst = vsu.getSignatureCerts(signedDocumentFile.getAbsolutePath());

        assertEquals(2, lst.size());
        assertEquals(TestProperties.S_CERT_B, lst.get(0));
        assertEquals(TestProperties.S_CERT_A, lst.get(1));
    }

    @Test
    public void test_E_SignPDFDefVizualization()
            throws Exception {

        PrivateKey privateKeyB = TestProperties.S_KEY_B;
        assertNotNull("Missing private key", privateKeyB);
        X509Certificate certB = TestProperties.S_CERT_B;
        assertNotNull("Missing private key Certificate", certB);

        SignUtils signPdf = new SignUtils(privateKeyB, certB);

        File documentFile = new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
        File signedDocumentFile;
        try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {
            assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);

            signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION_DEF);
            signPdf.signPDF(documentFile, signedDocumentFile, true, "Slovenia", "ZPP Delivery");

        }
    }

    @Test
    public void testWithSelfSignedCert() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, SignatureException, KeyStoreException, OperatorCreationException {
        ValidateSignatureUtils vsu = new ValidateSignatureUtils();

        long validity = 365 * 24 * 60 * 60L;
        Date endDate = new Date(new Date().getTime() + validity * 1000);
        final PrivateKeyCertPair privateKeyCertPair = generateTestKeystore(endDate);

        final SignUtils signUtils = new SignUtils(privateKeyCertPair.getPrivateKey(), privateKeyCertPair.getCertificate());

        File documentFile = new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
        File signedDocumentFile;
        try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {
            assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);

            signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION_DEF);
            signUtils.signPDF(documentFile, signedDocumentFile, true, "Slovenia", "ZPP Delivery");

            final List<SignatureInfo> signatureInfos = vsu.validateSignatures(signedDocumentFile);
            assertThat(signatureInfos.size(), is(1));
            assertThat(signatureInfos.get(0).isSignatureValid, is(true));
        }
    }

    @Test
    public void testWithExpiredSelfignedCert() throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, OperatorCreationException, SignatureException {
        ValidateSignatureUtils vsu = new ValidateSignatureUtils();
        Date endDate = new Date(new Date().getTime() - 50000);
        final PrivateKeyCertPair privateKeyCertPair = generateTestKeystore(endDate);

        final SignUtils signUtils = new SignUtils(privateKeyCertPair.getPrivateKey(), privateKeyCertPair.getCertificate());

        File documentFile = new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
        File signedDocumentFile;
        try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {
            assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);

            signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION_DEF);
            signUtils.signPDF(documentFile, signedDocumentFile, true, "Slovenia", "ZPP Delivery");

            final List<SignatureInfo> signatureInfos = vsu.validateSignatures(signedDocumentFile);
            assertThat(signatureInfos.size(), is(1));
            final SignatureInfo signatureInfo = signatureInfos.get(0);
            assertThat(signatureInfo.isSignatureValid, is(false));
            assertThat(signatureInfo.getErrorMessages(), is(notNullValue()));
            assertThat(signatureInfo.getErrorMessages().get(0), is(notNullValue()));
        }
    }

    @Test
    public void testValidateSignedPDF() throws IOException, CertificateException, NoSuchAlgorithmException {
        ValidateSignatureUtils vsu = new ValidateSignatureUtils();
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream adviceOfDelivery = classLoader.getResourceAsStream("advice-of-delivery-2023-08-31-167219@court-laurentius.si.pdf");
        final List<SignatureInfo> adviceOfDeliverySignatures = vsu.validateSignatures(adviceOfDelivery);

        assertThat(adviceOfDeliverySignatures.size(), is(3));
        assertThat(adviceOfDeliverySignatures.get(0).isSignatureValid, is(true));
        assertThat(adviceOfDeliverySignatures.get(1).isSignatureValid, is(true));
        assertThat(adviceOfDeliverySignatures.get(2).isSignatureValid, is(true));
    }


    private PrivateKeyCertPair generateTestKeystore(final Date endDate) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException, OperatorCreationException {
        final String keystorePath = "src/test/resources/certs/test-ss-keystore.jks";
        final String keystorePassword = "test1234";
        final String keyPassword = "key1234";
        final String alias = "test-selfsigned-alias";

        deleteIfExists(Paths.get(keystorePath));

        // Generate keypair
        final KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA");
        kpGenerator.initialize(2048);
        final KeyPair keyPair = kpGenerator.generateKeyPair();

        final X509Certificate x509Certificate = generateSelfSignedCert(keyPair, endDate);

        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword.toCharArray(), new Certificate[]{x509Certificate});

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }

        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        final PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword.toCharArray());
        final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        return new PrivateKeyCertPair(privateKey, certificate);
    }

    private X509Certificate generateSelfSignedCert(final KeyPair keyPair, final Date endDate) throws CertificateException, OperatorCreationException {
        final Date startDate = new Date(new Date().getTime() - 10000);

        final X500Name owner = new X500Name("CN=Test, OU=Test, O=Test, L=Test, ST=Test, C=Test");
        final BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        final JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(owner, serialNumber, startDate, endDate, owner, keyPair.getPublic());
        final ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());

        final X509CertificateHolder certHolder = certBuilder.build(contentSigner);

        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }
}
