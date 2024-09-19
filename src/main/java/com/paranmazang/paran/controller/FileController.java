package com.paranmazang.paran.controller;

import com.paranmazang.paran.model.domain.FileDeleteModel;
import com.paranmazang.paran.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/list/{refId}")
    public ResponseEntity<?> getPathByRefId(@PathVariable("refId") Long refId, @RequestParam("type") String type) {
        return ResponseEntity.ok(fileService.getPathList(refId, type))
                ;
    }

    @GetMapping("/one")
    public ResponseEntity<?> getImage(@RequestParam("path") String path) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(fileService.getFile(path));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(MultipartFile file, String type, Long refId) {
        return ResponseEntity.ok(fileService.uploadFile(file, type, refId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestBody FileDeleteModel model) {
        return ResponseEntity.ok(fileService.delete(model));
    }


}