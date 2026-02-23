package com.marineguard.service.member4;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/library}")
    private String uploadDir;

    private Path rootLocation;

    public void init() {
        rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Store a file and return its filename
     */
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // Check for invalid characters
            if (originalFilename.contains("..")) {
                throw new RuntimeException("Invalid file path: " + originalFilename);
            }

            // Generate unique filename
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    /**
     * Store a file with custom name
     */
    public String storeFile(MultipartFile file, String customFilename) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            String filename = StringUtils.cleanPath(customFilename);

            if (filename.contains("..")) {
                throw new RuntimeException("Invalid file path: " + filename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    /**
     * Load file as byte array
     */
    public byte[] loadFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    /**
     * Load file as InputStream
     */
    public InputStream loadFileAsInputStream(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(Iterable<String> filenames) {
        for (String filename : filenames) {
            deleteFile(filename);
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filename) {
        Path file = rootLocation.resolve(filename);
        return Files.exists(file);
    }

    /**
     * Get file size in bytes
     */
    public long getFileSize(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            return Files.size(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file size: " + filename, e);
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Get content type based on file extension
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        switch (extension) {
            case ".pdf":
                return "application/pdf";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".mp4":
                return "video/mp4";
            case ".mp3":
                return "audio/mpeg";
            case ".txt":
                return "text/plain";
            case ".html":
            case ".htm":
                return "text/html";
            case ".csv":
                return "text/csv";
            case ".doc":
            case ".docx":
                return "application/msword";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Generate thumbnail name from original filename
     */
    public String generateThumbnailName(String filename) {
        String baseName = filename.substring(0, filename.lastIndexOf("."));
        String extension = getFileExtension(filename);
        return baseName + "_thumb" + extension;
    }

    /**
     * Get full path of file
     */
    public Path getFilePath(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * Get upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Get relative URL for file access
     */
    public String getFileUrl(String filename) {
        return "/api/library/files/" + filename;
    }

    /**
     * Clean up old/unused files
     */
    public void cleanupOldFiles(int daysOld) {
        // Implementation for cleanup job
        // This would be called by a scheduled task
    }
}