package com.paranmanzang.paran.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.paranmanzang.paran.model.entity.File;
import com.paranmanzang.paran.model.domain.FileDeleteModel;
import com.paranmanzang.paran.model.enums.FileType;
import com.paranmanzang.paran.model.repository.FileRepository;
import com.paranmanzang.paran.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private final AmazonS3Client objectStorageClient;
    private final String BUCKET_NAME = "paran-test";

    @Override
    public Boolean uploadFile(MultipartFile file, String type, Long refId) {
        //        create folder
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        String folderName = type+"s/";
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, folderName, new ByteArrayInputStream(new byte[0]), objectMetadata);
        try {
            objectStorageClient.putObject(putObjectRequest);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

//        upload file
        try {
            var fileName = file.getOriginalFilename();
            var uploadName = folderName + UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));

            // MultipartFile의 InputStream을 사용하여 업로드
            objectStorageClient.putObject(BUCKET_NAME, uploadName, file.getInputStream(), new ObjectMetadata());
            reactiveMongoTemplate.insert(File.builder()
                    .path(uploadName)
                    .refId(refId)
                    .type(FileType.fromType(type).getCode())
                    .uploadAt(LocalDateTime.now())
                    .build()).block();
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public Mono<?> getPathList(Long refId, String type) {
        return fileRepository.findByRefId(refId, FileType.fromType(type).getCode()).then();
    }

    @Override
    public byte[] getFile(String path) throws IOException {
        return IOUtils.toByteArray(objectStorageClient
                .getObject(BUCKET_NAME, path)
                .getObjectContent());
    }

    @Override
    public Boolean delete(FileDeleteModel model) {
        objectStorageClient.deleteObject(BUCKET_NAME, model.getPath());
        fileRepository.deleteByPath(model.getPath()).block();
        return Boolean.TRUE;
    }
}
