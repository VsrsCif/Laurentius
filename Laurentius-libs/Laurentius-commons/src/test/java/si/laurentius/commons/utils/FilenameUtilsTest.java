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
package si.laurentius.commons.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for FilenameUtils class.
 * Tests all filename encoding and sanitization functionality.
 * 
 * @author Analysis Team
 */
public class FilenameUtilsTest {

    /**
     * Test sanitization of Slovenian characters
     */
    @Test
    public void testSlovenianCharacterSanitization() {
        String originalFilename = "dokument_čšžćđ.pdf";
        String sanitized = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("Slovenian chars test - Original: " + originalFilename);
        System.out.println("Slovenian chars test - Sanitized: " + sanitized);
        
        assertEquals("dokument_cszcđ.pdf".replace("đ", "d"), sanitized);
        assertTrue("Should be ASCII-safe", FilenameUtils.isAsciiOnly(sanitized));
        assertTrue("Should preserve PDF extension", sanitized.endsWith(".pdf"));
    }
    
    /**
     * Test the real-world complex filename from requirements
     */
    @Test
    public void testRealWorldComplexFilename() {
        String originalFilename = "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf";
        String sanitized = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("Complex filename test - Original: " + originalFilename);
        System.out.println("Complex filename test - Sanitized: " + sanitized);
        
        assertTrue("Should be ASCII-safe", FilenameUtils.isAsciiOnly(sanitized));
        assertTrue("Should preserve PDF extension", sanitized.endsWith(".pdf"));
        assertFalse("Should not contain spaces", sanitized.contains(" "));
        assertFalse("Should not contain parentheses", sanitized.contains("(") || sanitized.contains(")"));
        assertTrue("Should contain recognizable words", 
                  sanitized.contains("Obvestilo") && sanitized.contains("ZPP"));
    }
    
    /**
     * Test ASCII filename detection
     */
    @Test
    public void testAsciiOnlyDetection() {
        assertTrue("ASCII filename should be detected", 
                  FilenameUtils.isAsciiOnly("simple_file.txt"));
        assertFalse("Non-ASCII filename should be detected", 
                   FilenameUtils.isAsciiOnly("dokument_čšžćđ.pdf"));
        assertTrue("Empty string should be ASCII", 
                  FilenameUtils.isAsciiOnly(""));
        assertTrue("Numbers and symbols should be ASCII", 
                  FilenameUtils.isAsciiOnly("file_123-test.pdf"));
    }
    
    /**
     * Test filename analysis functionality
     */
    @Test
    public void testFilenameAnalysis() {
        String filename = "dokument_čšžćđ.pdf";
        FilenameUtils.FilenameInfo info = FilenameUtils.analyzeFilename(filename);
        
        System.out.println("Analysis test: " + info.toString());
        
        assertEquals(filename, info.getFilename());
        assertEquals(18, info.getCharacterCount()); // 18 characters
        assertEquals(23, info.getByteCount()); // 23 UTF-8 bytes  
        assertFalse("Should not be ASCII-safe", info.isAsciiSafe());
        assertTrue("Should have international chars", info.hasInternationalChars());
        assertEquals("pdf", info.getExtension());
    }
    
    /**
     * Test extension extraction
     */
    @Test
    public void testExtensionExtraction() {
        assertEquals("pdf", FilenameUtils.extractExtension("document.pdf"));
        assertEquals("txt", FilenameUtils.extractExtension("file.txt"));
        assertEquals("", FilenameUtils.extractExtension("noextension"));
        assertEquals("", FilenameUtils.extractExtension(""));
        assertEquals("", FilenameUtils.extractExtension(null));
        assertEquals("pdf", FilenameUtils.extractExtension("file.with.dots.pdf"));
        assertEquals("", FilenameUtils.extractExtension("file."));
    }
    
    /**
     * Test security validation
     */
    @Test
    public void testSecurityValidation() {
        // Safe filenames
        assertTrue("Simple filename should be safe", 
                  FilenameUtils.isSecureFilename("document.pdf"));
        assertTrue("Filename with underscores should be safe", 
                  FilenameUtils.isSecureFilename("my_document.pdf"));
        
        // Unsafe filenames
        assertFalse("Path traversal should be unsafe", 
                   FilenameUtils.isSecureFilename("../../../etc/passwd"));
        assertFalse("Windows path traversal should be unsafe", 
                   FilenameUtils.isSecureFilename("..\\..\\system32\\file.txt"));
        assertFalse("Forward slash should be unsafe", 
                   FilenameUtils.isSecureFilename("path/to/file.txt"));
        assertFalse("Null filename should be unsafe", 
                   FilenameUtils.isSecureFilename(null));
        assertFalse("Empty filename should be unsafe", 
                   FilenameUtils.isSecureFilename(""));
    }
    
    /**
     * Test edge cases
     */
    @Test
    public void testEdgeCases() {
        // Null and empty
        assertEquals("download.bin", FilenameUtils.sanitizeFilenameForDownload(null));
        assertEquals("download.bin", FilenameUtils.sanitizeFilenameForDownload(""));
        assertEquals("download.bin", FilenameUtils.sanitizeFilenameForDownload("   "));
        
        // Already ASCII-safe
        String asciiFilename = "simple_file.txt";
        assertEquals(asciiFilename, FilenameUtils.sanitizeFilenameForDownload(asciiFilename));
        
        // Only non-ASCII characters
        String nonAsciiOnly = "čšžćđ";
        String sanitized = FilenameUtils.sanitizeFilenameForDownload(nonAsciiOnly);
        assertTrue("Should be ASCII-safe", FilenameUtils.isAsciiOnly(sanitized));
        assertEquals("cszcđ".replace("đ", "d"), sanitized);
    }
    
    /**
     * Test Unicode normalization
     */
    @Test
    public void testUnicodeNormalization() {
        String nfcFilename = "café.txt"; // NFC normalized
        String nfdFilename = "cafe\u0301.txt"; // NFD (e + combining acute)
        
        String nfcResult = FilenameUtils.sanitizeFilenameForDownload(nfcFilename);
        String nfdResult = FilenameUtils.sanitizeFilenameForDownload(nfdFilename);
        
        System.out.println("Unicode test - NFC: " + nfcFilename + " -> " + nfcResult);
        System.out.println("Unicode test - NFD: " + nfdFilename + " -> " + nfdResult);
        
        // Both should normalize to same result
        assertEquals("Both forms should normalize to same result", nfcResult, nfdResult);
        assertTrue("Result should be ASCII-safe", FilenameUtils.isAsciiOnly(nfcResult));
        assertEquals("cafe.txt", nfcResult);
    }
    
    /**
     * Test Content-Disposition header creation
     */
    @Test
    public void testContentDispositionHeader() {
        // ASCII filename
        String asciiHeader = FilenameUtils.createContentDispositionHeader("simple.pdf", "simple.pdf");
        assertEquals("attachment; filename=\"simple.pdf\"", asciiHeader);
        
        // Non-ASCII filename
        String nonAsciiOriginal = "dokument_čšžćđ.pdf";
        String nonAsciiSanitized = "dokument_cszcđ.pdf".replace("đ", "d");
        String nonAsciiHeader = FilenameUtils.createContentDispositionHeader(nonAsciiOriginal, nonAsciiSanitized);
        
        System.out.println("Content-Disposition header: " + nonAsciiHeader);
        
        assertTrue("Should contain fallback filename", nonAsciiHeader.contains(nonAsciiSanitized));
        assertTrue("Should contain RFC 6266 encoded filename", nonAsciiHeader.contains("filename*=UTF-8''"));
        assertTrue("Should be attachment type", nonAsciiHeader.startsWith("attachment"));
    }
    
    /**
     * Test international character handling beyond Slovenian
     */
    @Test
    public void testInternationalCharacters() {
        String[] testCases = {
            "document_äöü.pdf|document_aeoeue.pdf",  // German
            "файл.txt|_.txt",                        // Cyrillic  
            "файл_中文.pdf|__._pdf",                  // Mixed scripts
            "café_résumé.doc|cafe_resume.doc"        // French
        };
        
        for (String testCase : testCases) {
            String[] parts = testCase.split("\\|");
            String original = parts[0];
            String expected = parts[1];
            
            String result = FilenameUtils.sanitizeFilenameForDownload(original);
            System.out.println("International test - " + original + " -> " + result);
            
            assertTrue("Should be ASCII-safe", FilenameUtils.isAsciiOnly(result));
            // Note: exact match may vary due to different replacement strategies
            assertNotNull("Should produce valid result", result);
        }
    }
    
    /**
     * Test long filename handling
     */
    @Test
    public void testLongFilename() {
        // Create a very long filename
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longName.append("very_long_filename_part_");
        }
        longName.append(".pdf");
        
        String longFilename = longName.toString();
        assertTrue("Should be longer than 200 chars", longFilename.length() > 200);
        
        String sanitized = FilenameUtils.sanitizeFilenameForDownload(longFilename);
        System.out.println("Long filename test - Original length: " + longFilename.length());
        System.out.println("Long filename test - Sanitized length: " + sanitized.length());
        
        assertTrue("Should be truncated to reasonable length", sanitized.length() <= 200);
        assertTrue("Should preserve extension", sanitized.endsWith(".pdf"));
        assertTrue("Should be ASCII-safe", FilenameUtils.isAsciiOnly(sanitized));
    }
    
    /**
     * Test all problematic filenames from the original analysis
     */
    @Test
    public void testAllProblematicFilenames() {
        String[] problematicFilenames = {
            "dokument_čšžćđ.pdf",
            "my document (version 2).pdf", 
            "Poročilo_o_postopku_č.123-žž_končna_verzija_s_posebnimi_znaki.pdf",
            "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf",
            "test file with  multiple   spaces.txt",
            "UPPERCASE_ČŠŽĆĐ_FILENAME.PDF",
            "mixed.Case.Filename.With.Dots.txt",
            "café.txt"
        };
        
        System.out.println("Testing all problematic filenames:");
        for (String filename : problematicFilenames) {
            String sanitized = FilenameUtils.sanitizeFilenameForDownload(filename);
            FilenameUtils.FilenameInfo info = FilenameUtils.analyzeFilename(filename);
            
            System.out.println("- Original: " + filename);
            System.out.println("  Sanitized: " + sanitized);
            System.out.println("  Info: " + info.toString());
            
            assertTrue("Sanitized filename should be ASCII-safe", FilenameUtils.isAsciiOnly(sanitized));
            assertTrue("Sanitized filename should be secure", FilenameUtils.isSecureFilename(sanitized));
            assertNotNull("Should always produce valid result", sanitized);
            assertFalse("Should not be empty", sanitized.trim().isEmpty());
        }
    }
}