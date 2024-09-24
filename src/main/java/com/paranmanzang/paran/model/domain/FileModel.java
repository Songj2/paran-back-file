package com.paranmanzang.paran.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FileModel {
    private String id;
    private String type;
    private String path;
    private long refId;
    private LocalDateTime uploadAt;
}
