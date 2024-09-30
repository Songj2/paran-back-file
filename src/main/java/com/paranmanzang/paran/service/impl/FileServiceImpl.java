package com.paranmanzang.paran.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.IOUtils;
import com.paranmanzang.paran.model.domain.FileModel;
import com.paranmanzang.paran.model.entity.File;
import com.paranmanzang.paran.model.domain.FileDeleteModel;
import com.paranmanzang.paran.model.enums.FileType;
import com.paranmanzang.paran.model.repository.FileRepository;
import com.paranmanzang.paran.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;


    @Value("${cloud.s3.bucket}")
    private String s3bucket;
    private final AmazonS3 amazonS3;

    public FileModel uploadFile(MultipartFile file, String type, Long refId) {
        String folderName = type + "s/";
        String fileName = file.getOriginalFilename();
        String uploadName = folderName + UUID.randomUUID() + Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));

        return Optional.of(new PutObjectRequest(s3bucket, folderName, new ByteArrayInputStream(new byte[0]), new ObjectMetadata()))
                .map(this::createFolder)
                .map(__ -> uploadFileToStorage(file, uploadName))
                .map(__ -> saveFileMetadata(uploadName, refId, type))
                .map(this::convertToFileModel)
                .orElseThrow(() -> new RuntimeException("Failed to upload file"));
    }

    private PutObjectResult createFolder(PutObjectRequest request) {
        try {
            return amazonS3.putObject(request);
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to create folder: " + e.getMessage(), e);
        }
    }

    private PutObjectResult uploadFileToStorage(MultipartFile file, String uploadName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            return amazonS3.putObject(s3bucket, uploadName, file.getInputStream(), metadata);
        } catch (IOException | SdkClientException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private File saveFileMetadata(String uploadName, Long refId, String type) {
        return Optional.of(File.builder()
                        .path(uploadName)
                        .refId(refId)
                        .type(FileType.fromType(type).getCode())
                        .uploadAt(LocalDateTime.now())
                        .build())
                .map(file -> reactiveMongoTemplate.insert(file).block())
                .orElseThrow(() -> new RuntimeException("Failed to save file metadata"));
    }


    @Override
    public List<?> getPathList(Long refId, String type) {
        return fileRepository.findByRefId(refId, FileType.fromType(type).getCode())
                .map(this::convertToFileModel)
                .collectList().block();
    }

    @Override
    public byte[] getFile(String path) throws IOException {
        return IOUtils.toByteArray(amazonS3
                .getObject(s3bucket, path)
                .getObjectContent());
    }

    @Override
    public Boolean delete(FileDeleteModel model) {
        amazonS3.deleteObject(s3bucket, model.getPath());
        fileRepository.deleteByPath(model.getPath()).block();
        return Boolean.TRUE;
    }

    private FileModel convertToFileModel(File file) {
        return Optional.ofNullable(file)
                .map(f -> FileModel.builder()
                        .id(f.getId())
                        .path(f.getPath())
                        .refId(f.getRefId())
                        .type(FileType.fromCode(f.getType()).getType())
                        .uploadAt(f.getUploadAt())
                        .build())
                .orElseThrow(() -> new RuntimeException("Failed to convert to FileModel"));
    }

    @Override
    public Mono<Void> transferFilesToS3() {
        var start= 740;
        var end= 1704;
        return fileRepository.findByType(FileType.fromType("book").getCode())
                .filter(file -> file.getRefId()>=start && file.getRefId()<=end)
                .flatMap(file -> Mono.fromCallable(() -> objectStorageClient().getObject(BUCKET_NAME, file.getPath()))
                        .subscribeOn(Schedulers.boundedElastic()) // 외부 I/O 작업 비동기 처리
                ).log()
                .flatMap(s3Object -> {
                    // 메타데이터 설정
                    System.out.println("NCloud에서 가져온 파일: " + s3Object.getKey());
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(s3Object.getObjectMetadata().getContentLength());
                    metadata.setContentType(s3Object.getObjectMetadata().getContentType());

                    // AWS S3에 업로드
                    return Mono.fromCallable(() -> {
                                amazonS3.putObject(
                                        s3bucket,           // AWS S3 버킷
                                        s3Object.getKey(),       // S3에서 사용할 파일의 키
                                        s3Object.getObjectContent(),  // 파일의 InputStream
                                        metadata                 // 파일 메타데이터
                                );
                                return Mono.empty();
                            })
                            .subscribeOn(Schedulers.boundedElastic())  // 외부 I/O 작업 비동기 처리
                            .retry(3) // 3회까지 재시도
                            .onErrorResume(e -> {
                                // 오류 처리 로직
                                System.err.println("S3 업로드 실패: " + e.getMessage());
                                return Mono.empty();
                            });
                }).log()
                .then(); // 작업이 완료되면 Void Mono 반환

    }

    private final String BUCKET_NAME = "paran-test";

    @Value("${ncp.storage.region}")
    private String ncRegion;

    @Value("${ncp.storage.endpoint}")
    private String ncEndPoint;

    @Value("${ncp.storage.accessKey}")
    private String ncAccessKey;

    @Value("${ncp.storage.secretKey}")
    private String ncSecretKey;


    public AmazonS3Client objectStorageClient() {
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ncEndPoint, ncRegion))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ncAccessKey, ncSecretKey)))
                .build();
    }

}
