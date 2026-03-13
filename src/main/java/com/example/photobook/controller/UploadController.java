package com.example.photobook.controller;

import com.example.photobook.dto.UploadDto;
import com.example.photobook.dto.UploadResponseDto;
import com.example.photobook.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {
    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDto> upload(
            @RequestParam("file") MultipartFile file,
            @ModelAttribute UploadDto dto) {
        return ResponseEntity.ok(uploadService.upload(file, dto));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        uploadService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
