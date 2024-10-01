package com.paranmanzang.paran.service;

import com.paranmanzang.paran.model.domain.FileDeleteModel;
import com.paranmanzang.paran.model.domain.FileModel;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

public interface FileService {
    FileModel uploadFile(MultipartFile file, String type, Long refId) throws IOException;
    List<?> getPathList(Long refId, String type);
    byte[] getFile(String path) throws IOException;
    byte[] getFileByRefId(Long refId, String type) throws IOException;
    Boolean delete(FileDeleteModel model);
    Mono<Void> transferFilesToS3();
}
