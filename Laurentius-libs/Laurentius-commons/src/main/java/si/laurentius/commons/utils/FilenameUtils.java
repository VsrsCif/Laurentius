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

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for handling filename encoding issues in Laurentius.
 * 
 * This class provides methods to:
 * - Sanitize filenames for HTTP Content-Disposition headers
 * - Handle Slovenian characters (čšžćđ) and other international characters
 * - Prevent download failures due to filename encoding issues
 * - Provide plugin API support for file access with special character filenames
 * 
 * @author Analysis Team
 */
public class FilenameUtils {
    
    private static final SEDLogger LOG = new SEDLogger(FilenameUtils.class);
    
    // Pattern for detecting illegal filename characters
    private static final Pattern ILLEGAL_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\x00-\\x1F]");
    
    // Maximum filename length for most filesystems
    private static final int MAX_FILENAME_LENGTH = 200;
    
    /**
     * Sanitize filename for safe HTTP header download.
     * Handles Slovenian characters and other special characters to prevent
     * HTTP 500 errors during download.
     * 
     * @param originalFilename Original filename that may contain special characters
     * @return Sanitized filename safe for HTTP Content-Disposition header
     */
    public static String sanitizeFilenameForDownload(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "download.bin";
        }
        
        // Log the sanitization attempt
        LOG.formatedDebug("Sanitizing filename for download: '%s'", originalFilename);
        
        // Check if filename is ASCII-safe (no encoding issues)
        boolean isAsciiSafe = isAsciiOnly(originalFilename);
        if (isAsciiSafe && originalFilename.length() <= MAX_FILENAME_LENGTH) {
            LOG.formatedDebug("Filename is already ASCII-safe: '%s'", originalFilename);
            return originalFilename; // No sanitization needed
        }
        
        // For non-ASCII filenames, create a safe fallback
        String sanitized = sanitizeInternationalCharacters(originalFilename);
        
        // Ensure length limits
        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = truncateFilename(sanitized, MAX_FILENAME_LENGTH);
        }
        
        LOG.formatedDebug("Sanitized filename: '%s' -> '%s'", originalFilename, sanitized);
        return sanitized;
    }
    
    /**
     * Check if filename contains only ASCII characters
     * 
     * @param filename Filename to check
     * @return true if filename contains only ASCII characters
     */
    public static boolean isAsciiOnly(String filename) {
        return filename != null && filename.chars().allMatch(c -> c < 128);
    }
    
    /**
     * Get filename encoding information for debugging
     * 
     * @param filename Filename to analyze
     * @return FilenameInfo object with encoding details
     */
    public static FilenameInfo analyzeFilename(String filename) {
        if (filename == null) {
            return new FilenameInfo("", 0, 0, true, false, "");
        }
        
        byte[] utf8Bytes = filename.getBytes(StandardCharsets.UTF_8);
        boolean isAscii = isAsciiOnly(filename);
        boolean hasInternationalChars = !isAscii;
        String extension = extractExtension(filename);
        
        return new FilenameInfo(filename, filename.length(), utf8Bytes.length, 
                               isAscii, hasInternationalChars, extension);
    }
    
    /**
     * Sanitize international characters while preserving readability
     * 
     * @param filename Original filename with international characters
     * @return Sanitized filename with ASCII equivalents
     */
    private static String sanitizeInternationalCharacters(String filename) {
        // First, normalize Unicode to NFC form
        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFC);
        
        // Replace Slovenian characters with closest ASCII equivalents
        String sanitized = normalized
            // Slovenian characters
            .replace("č", "c").replace("Č", "C")
            .replace("š", "s").replace("Š", "S") 
            .replace("ž", "z").replace("Ž", "Z")
            .replace("ć", "c").replace("Ć", "C")
            .replace("đ", "d").replace("Đ", "D")
            // Common international characters
            .replace("á", "a").replace("Á", "A")
            .replace("é", "e").replace("É", "E")
            .replace("í", "i").replace("Í", "I")
            .replace("ó", "o").replace("Ó", "O")
            .replace("ú", "u").replace("Ú", "U")
            .replace("ý", "y").replace("Ý", "Y")
            .replace("ň", "n").replace("Ň", "N")
            .replace("ř", "r").replace("Ř", "R")
            .replace("ť", "t").replace("Ť", "T")
            .replace("ů", "u").replace("Ů", "U")
            .replace("ä", "ae").replace("Ä", "AE")
            .replace("ö", "oe").replace("Ö", "OE")
            .replace("ü", "ue").replace("Ü", "UE")
            .replace("ß", "ss")
            // Replace problematic characters for HTTP headers
            .replace(" ", "_")        // Spaces to underscores
            .replace("(", "_").replace(")", "_")  // Parentheses
            .replace("[", "_").replace("]", "_")  // Brackets
            .replace("{", "_").replace("}", "_")  // Braces
            .replace("&", "and")      // Ampersand
            .replace("#", "_")        // Hash
            .replace("@", "_")        // At symbol
            // Remove any remaining non-ASCII characters
            .replaceAll("[^\\x00-\\x7F]", "_")
            // Clean up multiple underscores
            .replaceAll("_{2,}", "_")
            // Remove leading/trailing underscores
            .replaceAll("^_+|_+$", "");
        
        // Ensure we have a valid filename
        if (sanitized.isEmpty()) {
            sanitized = "download";
        }
        
        // Preserve the original extension if possible
        String originalExtension = extractExtension(filename);
        if (!originalExtension.isEmpty() && isValidExtension(originalExtension)) {
            if (!sanitized.contains(".")) {
                sanitized += "." + originalExtension;
            }
        }
        
        return sanitized;
    }
    
    /**
     * Extract file extension from filename
     * 
     * @param filename Filename to extract extension from
     * @return Extension without dot, or empty string if no extension
     */
    public static String extractExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        
        return "";
    }
    
    /**
     * Check if extension is valid (ASCII alphanumeric, reasonable length)
     * 
     * @param extension Extension to validate
     * @return true if extension is valid
     */
    private static boolean isValidExtension(String extension) {
        return extension != null && extension.matches("[a-zA-Z0-9]{1,10}");
    }
    
    /**
     * Truncate filename while preserving extension
     * 
     * @param filename Filename to truncate
     * @param maxLength Maximum length
     * @return Truncated filename
     */
    private static String truncateFilename(String filename, int maxLength) {
        if (filename.length() <= maxLength) {
            return filename;
        }
        
        String extension = extractExtension(filename);
        if (extension.isEmpty()) {
            return filename.substring(0, maxLength);
        }
        
        // Reserve space for extension and dot
        int availableLength = maxLength - extension.length() - 1;
        if (availableLength < 1) {
            return filename.substring(0, maxLength);
        }
        
        String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
        return nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), availableLength)) 
               + "." + extension;
    }
    
    /**
     * Create an RFC 6266 compliant Content-Disposition header value
     * 
     * @param originalFilename Original filename
     * @param sanitizedFilename Sanitized fallback filename
     * @return Content-Disposition header value
     */
    public static String createContentDispositionHeader(String originalFilename, String sanitizedFilename) {
        if (isAsciiOnly(originalFilename)) {
            return "attachment; filename=\"" + originalFilename + "\"";
        }
        
        // Use RFC 6266 filename* for non-ASCII filenames
        try {
            String encodedFilename = java.net.URLEncoder.encode(originalFilename, "UTF-8")
                                    .replace("+", "%20"); // Spaces should be %20 not +
            
            return "attachment; filename=\"" + sanitizedFilename + "\"; " +
                   "filename*=UTF-8''" + encodedFilename;
        } catch (Exception e) {
            LOG.formatedWarning("Failed to create RFC 6266 header for filename: %s", originalFilename);
            return "attachment; filename=\"" + sanitizedFilename + "\"";
        }
    }
    
    /**
     * Validate filename for security (prevent path traversal attacks)
     * 
     * @param filename Filename to validate
     * @return true if filename is safe
     */
    public static boolean isSecureFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }
        
        // Check for illegal characters
        if (ILLEGAL_FILENAME_CHARS.matcher(filename).find()) {
            return false;
        }
        
        // Check length
        if (filename.length() > MAX_FILENAME_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Information about filename encoding properties
     */
    public static class FilenameInfo {
        private final String filename;
        private final int characterCount;
        private final int byteCount;
        private final boolean isAsciiSafe;
        private final boolean hasInternationalChars;
        private final String extension;
        
        public FilenameInfo(String filename, int characterCount, int byteCount, 
                           boolean isAsciiSafe, boolean hasInternationalChars, String extension) {
            this.filename = filename;
            this.characterCount = characterCount;
            this.byteCount = byteCount;
            this.isAsciiSafe = isAsciiSafe;
            this.hasInternationalChars = hasInternationalChars;
            this.extension = extension;
        }
        
        // Getters
        public String getFilename() { return filename; }
        public int getCharacterCount() { return characterCount; }
        public int getByteCount() { return byteCount; }
        public boolean isAsciiSafe() { return isAsciiSafe; }
        public boolean hasInternationalChars() { return hasInternationalChars; }
        public String getExtension() { return extension; }
        
        @Override
        public String toString() {
            return String.format("FilenameInfo{filename='%s', chars=%d, bytes=%d, asciiSafe=%b, international=%b, ext='%s'}", 
                               filename, characterCount, byteCount, isAsciiSafe, hasInternationalChars, extension);
        }
    }
}