package com.paranmanzang.paran.model.repository.custom;

import com.paranmanzang.paran.model.entity.File;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileCustomRepository {
    Flux<File> findByRefId(Long refId, int type);
    Mono<?> findByPath(String path);
    Flux<File> findByType(int type);
    Mono<?> deleteByPath(String path);
}
