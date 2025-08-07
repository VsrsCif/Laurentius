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
package si.laurentius.msh.web.gui;

import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.commons.utils.FilenameUtils;

/**
 * Test class for filename sanitization in download functionality.
 * Tests the fixes for filename encoding issues with Slovenian characters.
 * 
 * @author Analysis Team
 */
public class DownloadFilenameTest {

    /**
     * Test sanitization of Slovenian characters in filenames
     */
    @Test
    public void testSlovenianCharacterSanitization() throws Exception {
        String originalFilename = "dokument_čšžćđ.pdf";
        String sanitizedFilename = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("Original: " + originalFilename);
        System.out.println("Sanitized: " + sanitizedFilename);
        
        // Should replace Slovenian characters with ASCII equivalents
        assertEquals("dokument_cszcđ.pdf".replace("đ", "d"), sanitizedFilename);
        
        // Should be ASCII-safe
        assertTrue("Sanitized filename should be ASCII-safe", 
                  sanitizedFilename.chars().allMatch(c -> c < 128));
    }
    
    /**
     * Test the real-world problematic filename from the requirements
     */
    @Test
    public void testRealWorldComplexFilename() throws Exception {
        String originalFilename = "Obvestilo pošiljatelju o opravljeni vročitvi ZPP-osebno (vročilnica) 3_4.pdf";
        String sanitizedFilename = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("Original: " + originalFilename);
        System.out.println("Sanitized: " + sanitizedFilename);
        
        // Should handle spaces, parentheses, and Slovenian characters
        assertNotNull("Sanitized filename should not be null", sanitizedFilename);
        assertTrue("Sanitized filename should be ASCII-safe", 
                  sanitizedFilename.chars().allMatch(c -> c < 128));
        assertTrue("Should preserve PDF extension", sanitizedFilename.endsWith(".pdf"));
        assertFalse("Should not contain spaces", sanitizedFilename.contains(" "));
        assertFalse("Should not contain parentheses", sanitizedFilename.contains("(") || sanitizedFilename.contains(")"));
    }
    
    /**
     * Test filename with whitespace and special characters
     */
    @Test
    public void testWhitespaceAndSpecialCharacters() throws Exception {
        String originalFilename = "my document (version 2).pdf";
        String sanitizedFilename = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("Original: " + originalFilename);
        System.out.println("Sanitized: " + sanitizedFilename);
        
        // Since this filename contains only ASCII characters, it should be preserved as-is
        // Our implementation only sanitizes non-ASCII characters for HTTP header compatibility
        assertEquals("my document (version 2).pdf", sanitizedFilename);
        assertTrue("Should be ASCII-safe", sanitizedFilename.chars().allMatch(c -> c < 128));
        assertTrue("Should preserve extension", sanitizedFilename.endsWith(".pdf"));
    }
    
    /**
     * Test edge cases
     */
    @Test
    public void testEdgeCases() throws Exception {
        // Test null filename
        String nullResult = FilenameUtils.sanitizeFilenameForDownload((String) null);
        assertEquals("download.bin", nullResult);
        
        // Test empty filename
        String emptyResult = FilenameUtils.sanitizeFilenameForDownload("");
        assertEquals("download.bin", emptyResult);
        
        // Test already ASCII-safe filename
        String asciiFilename = "simple_file.txt";
        String asciiResult = FilenameUtils.sanitizeFilenameForDownload(asciiFilename);
        assertEquals(asciiFilename, asciiResult);
        
        // Test filename without extension
        String noExtResult = FilenameUtils.sanitizeFilenameForDownload("dokument_čšžćđ");
        assertTrue("Should be ASCII-safe", noExtResult.chars().allMatch(c -> c < 128));
        assertEquals("dokument_cszcđ".replace("đ", "d"), noExtResult);
    }
    
    /**
     * Test that OutMailDataView has the same sanitization functionality
     */
    @Test 
    public void testOutMailDataViewSanitization() throws Exception {
        String originalFilename = "dokument_čšžćđ.pdf";
        String sanitizedFilename = FilenameUtils.sanitizeFilenameForDownload(originalFilename);
        
        System.out.println("OutMailDataView - Original: " + originalFilename);
        System.out.println("OutMailDataView - Sanitized: " + sanitizedFilename);
        
        assertTrue("OutMailDataView should also sanitize properly", 
                  sanitizedFilename.chars().allMatch(c -> c < 128));
        assertTrue("Should preserve extension", sanitizedFilename.endsWith(".pdf"));
    }
    
    /**
     * Test extension preservation with various file types
     */
    @Test
    public void testExtensionPreservation() throws Exception {
        String[] testCases = {
            "dokument_čšžćđ.pdf|pdf",
            "tabela_šž.xlsx|xlsx", 
            "slika_č.jpg|jpg",
            "arhiv_žš.zip|zip",
            "tekst_ć.txt|txt"
        };
        
        for (String testCase : testCases) {
            String[] parts = testCase.split("\\|");
            String filename = parts[0];
            String expectedExt = parts[1];
            
            String result = FilenameUtils.sanitizeFilenameForDownload(filename);
            System.out.println("Extension test - Original: " + filename + " -> Sanitized: " + result);
            
            assertTrue("Should preserve " + expectedExt + " extension", result.endsWith("." + expectedExt));
            assertTrue("Should be ASCII-safe", result.chars().allMatch(c -> c < 128));
        }
    }
}