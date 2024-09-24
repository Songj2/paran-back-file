package com.paranmanzang.paran.books;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.paranmanzang.paran.books.entity.Book;
import com.paranmanzang.paran.books.entity.BookImg;
import com.paranmanzang.paran.model.repository.BookImgRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
public class BookImgController {
    private final BookImgRepository bookImgRepository;
    private final BooksRepository booksRepository;

    private final AmazonS3Client objectStorageClient;

    @GetMapping("/books")
    public void crawling() {
        //알라딘
        String url = "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=1230&page=8&cnt=1000&SortOrder=1";
        try {
            var element = Jsoup.connect(url).get().select("#Myform");
            var img = element.select("img.front_cover").eachAttr("src");
            var title = element.select("a.bo3 b").eachText();
            var author = element.select("li:nth-child(3) > a:nth-child(1)").eachText();
            var publisher = element.select("li:nth-child(3) > a:nth-child(2)").eachText();

            save(img, author, publisher, title);
        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public void save(List<String> img, List<String> author, List<String> publisher, List<String> title) {
        IntStream.range(0, img.size()).forEach(i -> {
            try {
                bookImgRepository.insert(BookImg.builder().bookId(
                        booksRepository.save(Book.builder()
                                .title(title.get(i))
                                .author(author.get(i))
                                .categoryName("기타")
                                .publisher(publisher.get(i))
                                .build()).getId()
                ).path(fileProcess(img.get(i))
                ).build()).block();
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public String fileProcess(String url) throws IOException, URISyntaxException {
        String folderName = "books/";
        String bucketName = "paran-test";
        var uploadName = folderName + UUID.randomUUID() + url.substring(url.lastIndexOf("cover200/") + 9);
        objectStorageClient.putObject(new PutObjectRequest(bucketName, uploadName, new URL(url).openStream(), new ObjectMetadata()));
        return uploadName;
    }


}
