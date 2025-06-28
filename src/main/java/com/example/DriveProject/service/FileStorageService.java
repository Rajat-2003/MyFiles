package com.example.DriveProject.service;

import com.example.DriveProject.entity.FileEntity;
import com.example.DriveProject.repo.FileRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private FileRepo fileRepo;

    private Path fileStoragePath;

    @PostConstruct
    public void init() {
        try {
            this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();

            // Create directory if it doesn't exist
            if (!Files.exists(this.fileStoragePath)) {
                Files.createDirectories(this.fileStoragePath);
                System.out.println("✅ Created upload directory: " + this.fileStoragePath);
            } else {
                System.out.println("✅ Upload directory exists: " + this.fileStoragePath);
            }

            // Test write permissions
            Path testFile = this.fileStoragePath.resolve("test.txt");
            Files.write(testFile, "test".getBytes());
            Files.deleteIfExists(testFile);
            System.out.println("✅ Directory is writable");

        } catch (Exception e) {
            System.err.println("❌ Could not initialize file storage: " + e.getMessage());
            e.printStackTrace();

            // Fallback to system temp directory
            try {
                this.fileStoragePath = Paths.get(System.getProperty("java.io.tmpdir"), "drive-uploads")
                        .toAbsolutePath().normalize();
                Files.createDirectories(this.fileStoragePath);
                System.out.println("✅ Using fallback directory: " + this.fileStoragePath);
            } catch (Exception fallbackException) {
                System.err.println("❌ Fallback directory creation failed: " + fallbackException.getMessage());
                throw new RuntimeException("Cannot initialize file storage", fallbackException);
            }
        }
    }

    public String saveFile(MultipartFile file, Long parentFolderId) throws IOException {
        try {
            System.out.println("📁 Upload directory: " + this.fileStoragePath);
            System.out.println("📄 Original filename: " + file.getOriginalFilename());
            System.out.println("📊 File size: " + file.getSize() + " bytes");

            // Use the initialized path instead of creating new one
            String fileName = file.getOriginalFilename();
            Path filePath = this.fileStoragePath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Meta data for db
            FileEntity fileEntity = new FileEntity();
            fileEntity.setName(fileName);
            fileEntity.setPath(filePath.toString());
            fileEntity.setSize(file.getSize());
            fileEntity.setType("file");
            fileEntity.setParentFolderId(parentFolderId);
            fileEntity.setCreatedAt(LocalDateTime.now());

            fileRepo.save(fileEntity);

            return "File uploaded successfully !!";

        } catch (Exception e) {
            System.err.println("❌ Error saving file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public FileEntity getFileById(Long id) {
        return fileRepo.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public List<FileEntity> getFilesInFolder(Long parentFolderId) {
        if (parentFolderId == null) {
            return fileRepo.findAll()
                    .stream()
                    .filter(f -> f.getParentFolderId() == null)
                    .collect(Collectors.toList());
        } else {
            return fileRepo.findAll()
                    .stream()
                    .filter(f -> parentFolderId.equals(f.getParentFolderId()))
                    .collect(Collectors.toList());
        }
    }

    public void deleteById(Long id) {
        FileEntity fileEntity = getFileById(id);

        // Delete physical file
        try {
            Path filePath = Paths.get(fileEntity.getPath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log the error but continue with database deletion
            System.err.println("Could not delete physical file: " + e.getMessage());
        }

        // Delete from database
        fileRepo.deleteById(id);
    }
}