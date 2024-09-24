package com.paranmanzang.paran.model.repository;

import com.paranmanzang.paran.books.entity.BookImg;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookImgRepository extends ReactiveMongoRepository<BookImg, String> {
}
