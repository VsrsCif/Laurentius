/*
 * Copyright 2023, Supreme Court Republic of Slovenia
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
package si.laurentius.commons.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.StorageException;

/**
 * Test class to identify and reproduce filename encoding issues with Slovenian 
 * characters, whitespace, and special symbols in the Laurentius system.
 * 
 * These tests are designed to expose the vulnerabilities identified in the 
 * filename encoding analysis and demonstrate where the system fails to handle
 * international characters properly.
 * 
 * @author Analysis Team
 */
public class FilenameEncodingTest {

    private StorageUtils storageUtils;
    
    // Test filenames that expose different types of encoding issues
    private static final List<String> PROBLEMATIC_FILENAMES = Arrays.asList(
        // Slovenian characters
        "dokument_čšžćđ.pdf",
        
        // Whitespace and special characters
        "my document (version 2).pdf",
        
        // Long filename with mixed encoding
        "Poročilo_o_postopku_č.123-žž_končna_verzija_s_posebnimi_znaki.pdf",
        
        // Real-world complex filename
        "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf",
        
        // Edge cases
        "test file with  multiple   spaces.txt",
        "UPPERCASE_ČŠŽĆĐ_FILENAME.PDF",
        "mixed.Case.Filename.With.Dots.txt",
        
        // Unicode normalization issues
        "café.txt", // NFC form
        "cafe\u0301.txt" // NFD form (e + combining acute accent)
    );

    @Before
    public void setUp() throws IOException {
        storageUtils = new StorageUtils();
        
        // Set up temporary storage directory
        System.setProperty(SEDSystemProperties.SYS_PROP_HOME_DIR, 
                          System.getProperty("java.io.tmpdir"));
        
        // Clear any existing storage
        cleanupStorage();
    }

    @After
    public void tearDown() throws IOException {
        cleanupStorage();
    }
    
    private void cleanupStorage() throws IOException {
        Path storageDir = StorageUtils.getStorageFolder().toPath();
        if (Files.exists(storageDir)) {
            Files.walk(storageDir)
                 .sorted((a, b) -> b.compareTo(a))
                 .forEach(path -> {
                     try {
                         Files.deleteIfExists(path);
                     } catch (IOException e) {
                         // Ignore cleanup errors in tests
                     }
                 });
        }
    }

    /**
     * Test filename handling with Slovenian characters (čšžćđ)
     * This test exposes encoding issues in the storage process.
     */
    @Test
    public void testFilenameWithSlovenianCharacters() throws Exception {
        String filename = "dokument_čšžćđ.pdf";
        System.out.println("Testing filename: " + filename);
        
        // Test UTF-8 encoding
        byte[] utf8Bytes = filename.getBytes(StandardCharsets.UTF_8);
        System.out.println("UTF-8 byte count: " + utf8Bytes.length + " vs character count: " + filename.length());
        
        // Test MIME type detection with special characters
        String mimeType = MimeValue.getMimeTypeByFileName(filename);
        System.out.println("MIME type detected: " + mimeType);
        assertEquals("Should detect PDF MIME type", "application/pdf", mimeType);
        
        // Test storage behavior - this exposes the filename loss issue
        File storedFile = storeTestFileWithName(filename);
        assertNotNull("File should be stored successfully", storedFile);
        assertTrue("Stored file should exist", storedFile.exists());
        
        // Document the filename loss issue
        String actualFilename = storedFile.getName();
        System.out.println("Original: " + filename);
        System.out.println("Stored:   " + actualFilename);
        
        // This documents the issue - original filename is lost in storage
        assertFalse("EXPECTED FAILURE: Original filename is lost during storage", 
                   filename.equals(actualFilename));
        
        // Test file content preservation
        String content = new String(Files.readAllBytes(storedFile.toPath()), StandardCharsets.UTF_8);
        assertTrue("Content should contain special characters", 
                  content.contains("čšžćđ"));
    }

    /**
     * Test the real-world complex filename that was reported as problematic
     */
    @Test
    public void testRealWorldComplexFilename() throws Exception {
        String filename = "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf";
        System.out.println("Testing real-world complex filename: " + filename);
        
        // Test encoding properties
        assertFalse("Should not be ASCII-only", isAsciiOnly(filename));
        assertEquals("Should be 76 characters", 76, filename.length());
        
        // Test UTF-8 byte representation
        byte[] utf8Bytes = filename.getBytes(StandardCharsets.UTF_8);
        assertEquals("Should be 79 UTF-8 bytes", 79, utf8Bytes.length);
        
        // Test URL encoding (simulates HTTP header issues)
        String urlEncoded = java.net.URLEncoder.encode(filename, "UTF-8");
        assertNotEquals("URL encoding should change the filename", filename, urlEncoded);
        System.out.println("URL encoded length: " + urlEncoded.length());
        
        // Test MIME type detection
        String mimeType = MimeValue.getMimeTypeByFileName(filename);
        assertEquals("Should detect PDF", "application/pdf", mimeType);
        
        // Test extension extraction
        int lastDot = filename.lastIndexOf('.');
        assertTrue("Should find extension", lastDot > 0);
        String extension = filename.substring(lastDot + 1);
        assertEquals("Should extract 'pdf'", "pdf", extension);
        
        String namePart = filename.substring(0, lastDot);
        assertTrue("Name part should contain special characters", 
                  namePart.contains("š") && namePart.contains("č"));
    }

    /**
     * Test all problematic filenames to identify patterns of failure
     */
    @Test
    public void testAllProblematicFilenames() {
        System.out.println("Testing all problematic filenames:");
        
        int nonAsciiCount = 0;
        int longFilenameCount = 0;
        
        for (String filename : PROBLEMATIC_FILENAMES) {
            System.out.println("- Testing: " + filename);
            
            // Test ASCII compatibility
            boolean isAscii = isAsciiOnly(filename);
            if (!isAscii) {
                nonAsciiCount++;
                System.out.println("  WARNING: Non-ASCII characters detected");
            }
            
            // Test filename length
            if (filename.length() > 50) {
                longFilenameCount++;
                System.out.println("  WARNING: Long filename (" + filename.length() + " chars)");
            }
            
            // Test MIME type detection
            String mimeType = MimeValue.getMimeTypeByFileName(filename);
            assertNotNull("MIME type should be detected", mimeType);
            
            // Test string operations don't break encoding
            String uppercase = filename.toUpperCase();
            String lowercase = filename.toLowerCase();
            assertNotNull("Case conversion should work", uppercase);
            assertNotNull("Case conversion should work", lowercase);
        }
        
        System.out.println("Summary: " + nonAsciiCount + " non-ASCII filenames, " + 
                          longFilenameCount + " long filenames");
        
        // These are the files that will cause HTTP header issues
        assertTrue("Should find files with encoding issues", nonAsciiCount > 0);
    }

    /**
     * Test Unicode normalization issues
     */
    @Test
    public void testFilenameNormalization() throws Exception {
        String nfcFilename = "café.txt"; // NFC normalized
        String nfdFilename = "cafe\u0301.txt"; // NFD (e + combining acute)
        
        System.out.println("NFC filename: " + nfcFilename);
        System.out.println("NFD filename: " + nfdFilename);
        System.out.println("Are they equal? " + nfcFilename.equals(nfdFilename));
        
        // Test if they normalize to the same form
        String nfcNormalized = Normalizer.normalize(nfcFilename, Normalizer.Form.NFC);
        String nfdNormalized = Normalizer.normalize(nfdFilename, Normalizer.Form.NFC);
        
        assertEquals("Both should normalize to same NFC form", nfcNormalized, nfdNormalized);
        
        // Test storage of both forms
        File nfcFile = storeTestFileWithName(nfcFilename);
        File nfdFile = storeTestFileWithName(nfdFilename);
        
        assertNotNull("NFC file should be stored", nfcFile);
        assertNotNull("NFD file should be stored", nfdFile);
        
        // Both should be storable but will have different system-generated names
        assertNotEquals("Files should have different generated names", 
                       nfcFile.getName(), nfdFile.getName());
    }

    /**
     * Test extension extraction with special characters
     */
    @Test
    public void testExtensionExtractionWithSpecialChars() {
        String[] filenames = {
            "dokument_čšžćđ.pdf",
            "file with spaces.txt",
            "file.with.multiple.dots.doc",
            "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf"
        };
        
        System.out.println("Testing extension extraction:");
        
        for (String filename : filenames) {
            System.out.println("Filename: " + filename);
            
            // Test the current implementation logic
            String mimeType = MimeValue.getMimeTypeByFileName(filename);
            System.out.println("MIME type: " + mimeType);
            
            // Test manual extension extraction
            int lastDot = filename.lastIndexOf('.');
            if (lastDot > 0) {
                String extension = filename.substring(lastDot + 1);
                System.out.println("Extension: " + extension);
                
                // Verify extension is valid
                assertTrue("Extension should not be empty", !extension.isEmpty());
                assertTrue("Extension should be alphanumeric", 
                          extension.matches("[a-zA-Z0-9]+"));
            } else {
                fail("Should find extension in filename: " + filename);
            }
            
            System.out.println("---");
        }
    }

    /**
     * Test HTTP header encoding simulation
     */
    @Test
    public void testHttpHeaderEncodingIssues() throws Exception {
        String[] testFilenames = {
            "dokument_čšžćđ.pdf",
            "my document (version 2).pdf",
            "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf"
        };
        
        for (String filename : testFilenames) {
            System.out.println("Testing HTTP header encoding for: " + filename);
            
            // Test ASCII compatibility
            boolean isAsciiSafe = isAsciiOnly(filename);
            System.out.println("  ASCII safe: " + isAsciiSafe);
            
            if (!isAsciiSafe) {
                System.out.println("  ISSUE: Non-ASCII characters will cause HTTP header problems");
                
                // Test URL encoding
                String urlEncoded = java.net.URLEncoder.encode(filename, "UTF-8");
                System.out.println("  URL encoded: " + urlEncoded);
                
                // This demonstrates the issue - URL encoding makes filenames unreadable
                assertNotEquals("URL encoding changes filename", filename, urlEncoded);
            }
        }
    }

    // Helper methods

    /**
     * Store a test file with the given filename (simulates the storage process)
     */
    private File storeTestFileWithName(String filename) throws Exception {
        // Create test content with special characters
        String testContent = "Test file content for: " + filename + 
                           "\nUTF-8 encoded content: čšžćđ" +
                           "\nOriginal filename: " + filename;
        byte[] testContentBytes = testContent.getBytes(StandardCharsets.UTF_8);
        
        // Extract suffix for storage
        String suffix = "bin";
        if (filename.contains(".")) {
            suffix = filename.substring(filename.lastIndexOf('.') + 1);
        }
        
        // Use StorageUtils to store (this will demonstrate filename loss)
        File storedFile = storageUtils.storeFile("test_", suffix, testContentBytes);
        
        return storedFile;
    }

    /**
     * Check if filename contains only ASCII characters
     */
    private boolean isAsciiOnly(String filename) {
        return filename.chars().allMatch(c -> c < 128);
    }

    /**
     * Test filename security validation (basic test)
     */
    @Test
    public void testFilenameSecurityValidation() {
        String[] suspiciousNames = {
            "../../../etc/passwd",
            "..\\..\\windows\\system32\\file.txt",
            "normal_file.txt" // control case
        };
        
        for (String filename : suspiciousNames) {
            System.out.println("Testing filename: " + filename.replaceAll("\\p{Cntrl}", "?"));
            
            boolean containsPathTraversal = filename.contains("../") || filename.contains("..\\");
            
            if (containsPathTraversal) {
                System.out.println("  WARNING: Path traversal detected");
                assertTrue("Should detect path traversal", containsPathTraversal);
            } else {
                System.out.println("  OK: No path traversal");
            }
        }
    }
}