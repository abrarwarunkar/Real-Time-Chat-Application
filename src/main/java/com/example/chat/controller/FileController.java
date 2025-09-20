package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file, 
            Authentication auth) {
        try {
            User user = (User) auth.getPrincipal();
            String fileUrl = fileService.uploadFile(file, user.getId());
            
            return ResponseEntity.ok(Map.of(
                "url", fileUrl,
                "filename", file.getOriginalFilename(),
                "contentType", file.getContentType(),
                "size", String.valueOf(file.getSize())
            ));
        } catch (Exception e) {
            logger.error("File upload failed for user {}: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "File upload failed"));
        }
    }
}