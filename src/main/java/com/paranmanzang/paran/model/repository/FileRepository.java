package com.paranmanzang.paran.model.repository;

import com.paranmanzang.paran.model.entity.File;
import com.paranmanzang.paran.model.repository.custom.FileCustomRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends ReactiveMongoRepository<File, String>, FileCustomRepository {
}
