package com.project.snm.repository;

import com.project.snm.model.mongo.ContentBlob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentBlobRepository extends MongoRepository<ContentBlob, String> {
}