package com.example.DriveProject.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    private String name;
    private String type;

    public FileEntity() {
    }

    private String path;
    private Long size ;
    private LocalDateTime CreatedAt;
    private Long parentFolderId;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public LocalDateTime getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        CreatedAt = createdAt;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileEntity(long id, String name, String type, String path, Long size, LocalDateTime createdAt, Long parentFolderId) {
        Id = id;
        this.name = name;
        this.type = type;
        this.path = path;
        this.size = size;
        CreatedAt = createdAt;
        this.parentFolderId = parentFolderId;
    }
}
