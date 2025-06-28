package com.example.DriveProject.controller;

import com.example.DriveProject.entity.FileEntity;
import com.example.DriveProject.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;



@RestController
@RequestMapping("/api/files")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "http://mydrive-xi.vercel.app"
        },
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true"
)
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    // Upload file endpoint
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {

        System.out.println("Upload request received");
        System.out.println("File is null: " + (file == null));
        if (file != null) {
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("File empty: " + file.isEmpty());
        }
        System.out.println("Parent folder ID: " + parentFolderId);

        try {
            if (file == null) {
                return ResponseEntity.badRequest().body("No file provided. Make sure the form field name is 'file'");
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            String response = fileStorageService.saveFile(file, parentFolderId);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unexpected error occurred: " + e.getMessage());
        }
    }

    // Download file endpoint
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileStorageService.getFileById(id);
            Path path = Paths.get(fileEntity.getPath());

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Try to determine file's content type
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileEntity.getName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Get all files in a folder (or root if parentFolderId is null)
    @GetMapping
    public ResponseEntity<List<FileEntity>> getFiles(@RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {
        try {
            List<FileEntity> files = fileStorageService.getFilesInFolder(parentFolderId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Get file details by ID
    @GetMapping("/{id}")
    public ResponseEntity<FileEntity> getFileById(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileStorageService.getFileById(id);
            return ResponseEntity.ok(fileEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Delete file endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileStorageService.deleteById(id);
            return ResponseEntity.ok("File deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting file: " + e.getMessage());
        }
    }

    // Preview file endpoint (for images, text files, etc.)
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileStorageService.getFileById(id);
            Path path = Paths.get(fileEntity.getPath());

            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileEntity.getName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Get file metadata without downloading
    @GetMapping("/info/{id}")
    public ResponseEntity<FileEntity> getFileInfo(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileStorageService.getFileById(id);
            return ResponseEntity.ok(fileEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Search files by name
    @GetMapping("/search")
    public ResponseEntity<List<FileEntity>> searchFiles(@RequestParam String query) {
        try {
            // This would require adding a search method to your service
            // For now, returning empty list with a TODO comment
            // TODO: Implement search functionality in FileStorageService
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}