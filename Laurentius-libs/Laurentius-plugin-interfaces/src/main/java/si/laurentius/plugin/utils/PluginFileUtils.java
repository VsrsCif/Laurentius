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
package si.laurentius.plugin.utils;

import java.io.File;
import java.io.IOException;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.FilenameUtils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.payload.MSHOutPart;

/**
 * Utility class for plugin file handling with proper support for
 * international characters and filename encoding issues.
 * 
 * This class provides safe file access methods for plugins that handle
 * files with Slovenian characters and other special characters.
 * 
 * @author Analysis Team
 */
public class PluginFileUtils {

    /**
     * Safely get file from MSHInPart with proper filename handling.
     * Uses internal filepath for file access while preserving original filename info.
     * 
     * @param inPart Incoming mail part containing file reference
     * @return File object for actual file access
     * @throws IOException if file cannot be accessed
     */
    public static File getFileFromInPart(MSHInPart inPart) throws IOException {
        if (inPart == null || inPart.getFilepath() == null) {
            throw new IOException("Invalid MSHInPart or filepath is null");
        }
        
        // Use internal filepath for file system access (this is ASCII-safe)
        File file = StorageUtils.getFile(inPart.getFilepath());
        
        if (!file.exists()) {
            throw new IOException("File not found at path: " + inPart.getFilepath() + 
                                 " (original filename: " + inPart.getFilename() + ")");
        }
        
        return file;
    }
    
    /**
     * Safely get file from MSHOutPart with proper filename handling.
     * Uses internal filepath for file access while preserving original filename info.
     * 
     * @param outPart Outgoing mail part containing file reference
     * @return File object for actual file access  
     * @throws IOException if file cannot be accessed
     */
    public static File getFileFromOutPart(MSHOutPart outPart) throws IOException {
        if (outPart == null || outPart.getFilepath() == null) {
            throw new IOException("Invalid MSHOutPart or filepath is null");
        }
        
        // Use internal filepath for file system access (this is ASCII-safe)
        File file = StorageUtils.getFile(outPart.getFilepath());
        
        if (!file.exists()) {
            throw new IOException("File not found at path: " + outPart.getFilepath() + 
                                 " (original filename: " + outPart.getFilename() + ")");
        }
        
        return file;
    }
    
    /**
     * Get safe filename for plugin operations.
     * Returns ASCII-safe filename suitable for file system operations,
     * while preserving original filename information in metadata.
     * 
     * @param originalFilename Original filename with potential special characters
     * @return ASCII-safe filename suitable for file operations
     */
    public static String getSafeFilename(String originalFilename) {
        return FilenameUtils.sanitizeFilenameForDownload(originalFilename);
    }
    
    /**
     * Copy file with safe filename handling.
     * Copies file from MSH part to target directory with sanitized filename.
     * 
     * @param inPart Source MSH part
     * @param targetDirectory Target directory for copy
     * @param preserveOriginalName If true, use original filename; if false, use sanitized filename
     * @return File object of the copied file
     * @throws IOException if copy operation fails
     */
    public static File copyFileFromInPart(MSHInPart inPart, File targetDirectory, boolean preserveOriginalName) 
            throws IOException {
        File sourceFile = getFileFromInPart(inPart);
        
        String filename = preserveOriginalName ? inPart.getFilename() : getSafeFilename(inPart.getFilename());
        File targetFile = new File(targetDirectory, filename);
        
        // Ensure target directory exists
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Failed to create target directory: " + targetDirectory.getAbsolutePath());
        }
        
        // Copy file using StorageUtils
        try {
            StorageUtils.copyFile(sourceFile, targetFile, true);
        } catch (si.laurentius.commons.exception.StorageException ex) {
            throw new IOException("Failed to copy file: " + ex.getMessage(), ex);
        }
        
        return targetFile;
    }
    
    /**
     * Copy file with safe filename handling from outgoing mail part.
     * 
     * @param outPart Source MSH part
     * @param targetDirectory Target directory for copy
     * @param preserveOriginalName If true, use original filename; if false, use sanitized filename
     * @return File object of the copied file
     * @throws IOException if copy operation fails
     */
    public static File copyFileFromOutPart(MSHOutPart outPart, File targetDirectory, boolean preserveOriginalName) 
            throws IOException {
        File sourceFile = getFileFromOutPart(outPart);
        
        String filename = preserveOriginalName ? outPart.getFilename() : getSafeFilename(outPart.getFilename());
        File targetFile = new File(targetDirectory, filename);
        
        // Ensure target directory exists
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Failed to create target directory: " + targetDirectory.getAbsolutePath());
        }
        
        // Copy file using StorageUtils
        try {
            StorageUtils.copyFile(sourceFile, targetFile, true);
        } catch (si.laurentius.commons.exception.StorageException ex) {
            throw new IOException("Failed to copy file: " + ex.getMessage(), ex);
        }
        
        return targetFile;
    }
    
    /**
     * Get filename information for debugging and logging.
     * Provides detailed information about filename encoding issues.
     * 
     * @param filename Filename to analyze
     * @return FilenameInfo object with encoding details
     */
    public static FilenameUtils.FilenameInfo analyzeFilename(String filename) {
        return FilenameUtils.analyzeFilename(filename);
    }
    
    /**
     * Check if filename will cause encoding issues for plugins.
     * 
     * @param filename Filename to check
     * @return true if filename may cause encoding issues
     */
    public static boolean hasEncodingIssues(String filename) {
        if (filename == null) return false;
        
        FilenameUtils.FilenameInfo info = FilenameUtils.analyzeFilename(filename);
        return info.hasInternationalChars() || !FilenameUtils.isSecureFilename(filename);
    }
    
    /**
     * Create plugin-safe working directory with proper filename handling.
     * Creates a temporary directory for plugin operations with files that have special characters.
     * 
     * @param pluginName Name of the plugin (for directory naming)
     * @return File object representing the working directory
     * @throws IOException if directory creation fails
     */
    public static File createPluginWorkingDirectory(String pluginName) throws IOException {
        String safePluginName = FilenameUtils.sanitizeFilenameForDownload(pluginName);
        File workingDir = new File(System.getProperty("java.io.tmpdir"), 
                                  "laurentius-plugin-" + safePluginName + "-" + System.currentTimeMillis());
        
        if (!workingDir.mkdirs()) {
            throw new IOException("Failed to create plugin working directory: " + workingDir.getAbsolutePath());
        }
        
        return workingDir;
    }
    
    /**
     * Cleanup plugin working directory and all contained files.
     * Safe cleanup that handles files with special character names.
     * 
     * @param workingDirectory Directory to cleanup
     * @return true if cleanup was successful
     */
    public static boolean cleanupWorkingDirectory(File workingDirectory) {
        if (workingDirectory == null || !workingDirectory.exists()) {
            return true;
        }
        
        try {
            return deleteDirectoryRecursive(workingDirectory);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Recursively delete directory and all contents.
     * 
     * @param directory Directory to delete
     * @return true if successful
     */
    private static boolean deleteDirectoryRecursive(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectoryRecursive(file)) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }
}