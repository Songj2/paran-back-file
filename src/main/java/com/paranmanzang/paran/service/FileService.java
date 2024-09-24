package com.paranmanzang.paran.service;

import com.paranmanzang.paran.model.domain.FileDeleteModel;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

public interface FileService {
    Boolean uploadFile(MultipartFile file, String type, Long refId);
    Mono<?> getPathList(Long refId, String type);
    byte[] getFile(String path) throws IOException;
    Boolean delete(FileDeleteModel model);
}
