package com.myapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Handles data persistence operations for the application.
 * Provides methods for saving, loading, and backing up user data.
 */
public class DataManager {
    public static final String DATA_DIR = "data";
    public static final String BACKUP_DIR = DATA_DIR + "/backups";
    public static final String CACHE_DIR = DATA_DIR + "/cache";
    
    /**
     * Initialize data directories
     */
    public static void initializeDataDirectories() {
        // Ensure data directory exists
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        // Ensure backups directory exists
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
        
        // Ensure cache directory exists
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
    }
    
    /**
     * Creates a backup of a file
     * @param filePath Path to the file to backup
     * @throws IOException If backup fails
     */
    public static void createBackup(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = filePath.getFileName().toString();
            Path backupPath = Paths.get(BACKUP_DIR, "backup_" + timestamp + "_" + fileName);
            Files.copy(filePath, backupPath);
            
            // Limit number of backups to 10 per file by removing oldest if needed
            File backupFolder = new File(BACKUP_DIR);
            String filePrefix = "backup_";
            String fileSuffix = "_" + fileName;
            
            File[] backupFiles = backupFolder.listFiles((dir, name) -> 
                name.startsWith(filePrefix) && name.endsWith(fileSuffix));
            
            if (backupFiles != null && backupFiles.length > 10) {
                // Sort by last modified (oldest first)
                java.util.Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));
                
                // Delete oldest files until we have 10 or fewer
                for (int i = 0; i < backupFiles.length - 10; i++) {
                    backupFiles[i].delete();
                }
            }
        }
    }
    
    /**
     * Saves JSON data to a file with backup
     * @param data JSONObject or JSONArray to save
     * @param filePath Path to save the file
     * @throws IOException If saving fails
     */
    public static void saveJsonData(Object data, Path filePath) throws IOException {
        // Create backup of existing file
        if (Files.exists(filePath)) {
            createBackup(filePath);
        }
        
        // Write new data
        Files.write(filePath, data.toString().getBytes());
    }
    
    /**
     * Loads JSON data from a file
     * @param filePath Path to the JSON file
     * @return String containing the JSON data
     * @throws IOException If loading fails
     */
    public static String loadJsonData(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        
        return new String(Files.readAllBytes(filePath));
    }
    
    /**
     * Restores a file from its most recent backup
     * @param fileName Name of the file to restore
     * @return true if restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(String fileName) {
        try {
            File backupFolder = new File(BACKUP_DIR);
            String filePrefix = "backup_";
            String fileSuffix = "_" + fileName;
            
            File[] backupFiles = backupFolder.listFiles((dir, name) -> 
                name.startsWith(filePrefix) && name.endsWith(fileSuffix));
            
            if (backupFiles != null && backupFiles.length > 0) {
                // Sort by last modified (newest first)
                java.util.Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified).reversed());
                
                // Use the most recent backup
                Path backupPath = backupFiles[0].toPath();
                Path targetPath = Paths.get(DATA_DIR, fileName);
                
                Files.copy(backupPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to restore from backup: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Gets a list of available backups for a file
     * @param fileName Name of the file
     * @return Array of backup file names with timestamps
     */
    public static String[] getAvailableBackups(String fileName) {
        File backupFolder = new File(BACKUP_DIR);
        String filePrefix = "backup_";
        String fileSuffix = "_" + fileName;
        
        File[] backupFiles = backupFolder.listFiles((dir, name) -> 
            name.startsWith(filePrefix) && name.endsWith(fileSuffix));
        
        if (backupFiles != null && backupFiles.length > 0) {
            // Sort by last modified (newest first)
            java.util.Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified).reversed());
            
            String[] backupNames = new String[backupFiles.length];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (int i = 0; i < backupFiles.length; i++) {
                File file = backupFiles[i];
                try {
                    LocalDateTime modifiedTime = LocalDateTime.ofInstant(
                        Files.getLastModifiedTime(file.toPath()).toInstant(),
                        java.time.ZoneId.systemDefault()
                    );
                    
                    backupNames[i] = formatter.format(modifiedTime) + " - " + file.getName();
                } catch (IOException e) {
                    // If we can't get modified time, just use the filename
                    backupNames[i] = file.getName();
                }
            }
            
            return backupNames;
        }
        
        return new String[0];
    }
} 