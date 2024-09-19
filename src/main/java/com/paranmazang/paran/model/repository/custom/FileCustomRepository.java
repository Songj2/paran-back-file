package com.paranmazang.paran.model.repository.custom;

import com.paranmazang.paran.model.entity.File;

import java.util.List;

public interface FileCustomRepository {
    List<?> findByRefId(Long refId, int type);
    File findByPath(String path);
}
