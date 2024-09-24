package com.paranmanzang.paran.books.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "BookImages")
public class BookImg {
    @Id
    private String id;
    private String path;
    private Long bookId;
}
