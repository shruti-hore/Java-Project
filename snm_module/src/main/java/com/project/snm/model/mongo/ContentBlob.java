package com.project.snm.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "content_blobs")
public class ContentBlob {

    @Id
    private String id;

    private String encryptedContent;

    private String contentHash;

    private Instant createdAt;
}