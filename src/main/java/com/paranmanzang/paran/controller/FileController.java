package com.paranmanzang.paran.controller;

import com.paranmanzang.paran.model.domain.FileDeleteModel;
import com.paranmanzang.paran.service.FileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@CrossOrigin
@Tag(name = "01. File")
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileServiceImpl fileService;

    @GetMapping("/list/{refId}")
    @Operation(summary = "리스트 조회", description = "type의 refId인 파일 path 리스트를 조회합니다.", tags = {"01. File",})
    public ResponseEntity<?> getPathByRefId(@PathVariable("refId") Long refId, @RequestParam("type") String type) {
        return ResponseEntity.ok(fileService.getPathList(refId, type));
    }

    @GetMapping("/one")
    @Operation(summary = "파일 로드", description = "path에 해당하는 파일을 불러옵니다.")
    public ResponseEntity<?> getImage(@RequestParam("path") String path) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(fileService.getFile(path));
    }

    @PostMapping("/upload")
    @Operation(summary = "파일 저장", description = "파일을 업로드하고 파일정보를 db에 저장합니다.")
    public ResponseEntity<?> uploadFile(MultipartFile file, String type, Long refId) {
        return ResponseEntity.ok(fileService.uploadFile(file, type, refId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "파일 삭제", description = "파일 클라우드 상에서 삭제하고 db 정보도 삭제합니다.")
    public ResponseEntity<?> deleteFile(@RequestBody FileDeleteModel model) {
        return ResponseEntity.ok(fileService.delete(model));
    }


}