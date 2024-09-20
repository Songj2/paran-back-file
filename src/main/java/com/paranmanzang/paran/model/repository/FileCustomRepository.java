package com.paranmanzang.paran.model.repository;

import com.paranmanzang.paran.model.entity.File;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileCustomRepository extends ReactiveMongoRepository<File, String>, com.paranmanzang.paran.model.repository.custom.FileCustomRepository {
}
