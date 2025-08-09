/*
 * Copyright 2025, Supreme Court Republic of Slovenia
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
package si.jrc.msh.plugin.zpp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import si.laurentius.lce.sign.pdf.SignatureInfo;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;

/**
 * Simplified test class that reproduces the ZPP_A PDF signature validation issue.
 * 
 * This test focuses on the core PDF signature validation problem without 
 * complex mocking dependencies.
 * 
 * @author Jože Rihtaršič
 */
public class ZPPIssuesReproductionTest {

    private static final String PROBLEMATIC_PDF_FILE = "../../Laurentius-libs/Laurentius-lce/src/test/resources/triple_signed.pdf";
    
    private ValidateSignatureUtils signatureValidator;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Verify the problematic PDF exists (extracted from advice_of_delivery)
        File pdfFile = new File(PROBLEMATIC_PDF_FILE);
        if (!pdfFile.exists()) {
            throw new RuntimeException("Problematic PDF not found at: " + PROBLEMATIC_PDF_FILE + 
                ". Please ensure the PDF has been extracted from advice_of_delivery file.");
        }
    }

    @Before
    public void setUp() {
        signatureValidator = new ValidateSignatureUtils();
    }

    /**
     * Test PDF Signature Validation with BouncyCastle Compatibility
     * 
     * This test verifies that PDF signature validation handles compatibility issues gracefully.
     */
    @Test
    public void testPDFSignatureValidationWithBouncyCastle() throws Exception {
        File problematicPdf = new File(PROBLEMATIC_PDF_FILE);
        assertTrue("Problematic PDF test file should exist", problematicPdf.exists());

        // Attempt PDF signature validation
        List<SignatureInfo> signatures = signatureValidator.validateSignatures(problematicPdf);
        
        System.out.println("✓ PDF signature validation completed");
        System.out.println("Found " + signatures.size() + " signatures");
        
        // Check that we found signatures
        assertTrue("Should find at least one signature", signatures.size() > 0);
        
        // Check that all signatures are valid
        for (int i = 0; i < signatures.size(); i++) {
            SignatureInfo sig = signatures.get(i);
            System.out.println("Signature " + (i + 1) + ": valid=" + sig.isIsSignatureValid());
            if (!sig.isIsSignatureValid() && !sig.getErrorMessages().isEmpty()) {
                System.out.println("  Error messages: " + String.join(", ", sig.getErrorMessages()));
            }
            assertTrue("Signature " + (i + 1) + " should be valid", sig.isIsSignatureValid());
        }
    }

    /**
     * Note: Additional tests for RefToMessageId issue are available in:
     * Laurentius-msh-cxf/src/test/java/si/jrc/msh/interceptor/RefToMessageIdIssueTest.java
     * 
     * This simplified test focuses only on PDF signature validation to avoid 
     * complex mocking dependencies.
     */
    @Test 
    public void testDocumentationNote() {
        System.out.println("✓ ZPP_A issues are comprehensively tested in:");
        System.out.println("  - PDF validation: ValidateSignatureUtilsBounycCastleTest.java");
        System.out.println("  - RefToMessageId: RefToMessageIdIssueTest.java");
        assertTrue("Documentation test always passes", true);
    }
}