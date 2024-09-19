package com.paranmazang.paran.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.paranmazang.paran.model.domain.FileDeleteModel;
import com.paranmazang.paran.model.entity.File;
import com.paranmazang.paran.model.enums.FileType;
import com.paranmazang.paran.model.repository.FileRepository;
import com.paranmazang.paran.model.repository.impl.FileRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileRepositoryImpl fileRepositoryImpl;
    private final AmazonS3Client objectStorageClient;
    private final String BUCKET_NAME = "paran-test";
    private final String FOLDER_NAME = "test/";


    public Boolean uploadFile(MultipartFile file, String type, Long refId) {
//        create folder
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, FOLDER_NAME, new ByteArrayInputStream(new byte[0]), objectMetadata);
        try {
            objectStorageClient.putObject(putObjectRequest);
            System.out.format("Folder %s has been created.\n", FOLDER_NAME);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

//        upload file
        try {
            var fileName = file.getOriginalFilename();
            var uploadName = FOLDER_NAME + UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));

            // MultipartFile의 InputStream을 사용하여 업로드
            objectStorageClient.putObject(BUCKET_NAME, uploadName, file.getInputStream(), new ObjectMetadata());

            fileRepository.save(File.builder()
                    .path(uploadName)
                    .refId(refId)
                    .type(FileType.fromType(type).getCode())
                    .build());
        } catch (SdkClientException | IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public List<?> getPathList(Long refId, String type) {
        return fileRepositoryImpl.findByRefId(refId, FileType.fromType(type).getCode());

    }

    public byte[] getFile(String path) throws IOException {

        return IOUtils.toByteArray(objectStorageClient
                .getObject(BUCKET_NAME, path)
                .getObjectContent());
    }

    public Boolean delete(FileDeleteModel model) {
        objectStorageClient.deleteObject(BUCKET_NAME, model.getPath());
        fileRepository.delete(fileRepositoryImpl.findByPath(model.getPath()));
        return Boolean.TRUE;
    }

}