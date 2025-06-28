package com.example.DriveProject.repo;

import com.example.DriveProject.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FileRepo extends JpaRepository<FileEntity, Long> {

}
