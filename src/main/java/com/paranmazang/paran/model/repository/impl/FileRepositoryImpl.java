package com.paranmazang.paran.model.repository.impl;

import com.paranmazang.paran.model.entity.File;
import com.paranmazang.paran.model.entity.QFile;
import com.paranmazang.paran.model.repository.custom.FileCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<?> findByRefId(Long refId, int type) {
        QFile file = QFile.file;
        return jpaQueryFactory.select(file.path)
                .from(file)
                .where(file.refId.eq(refId)
                        .and(file.type.eq(type))
                ).fetch();
    }

    @Override
    public File findByPath(String path) {
        QFile file= QFile.file;
        return jpaQueryFactory.selectFrom(file).where(file.path.eq(path)).fetchFirst();
    }
}
