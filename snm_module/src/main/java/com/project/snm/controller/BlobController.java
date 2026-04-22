package com.project.snm.controller;

import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.model.mongo.ContentBlob;
import com.project.snm.service.BlobService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/blobs")
public class BlobController {

    private final BlobService blobService;

    public BlobController(BlobService blobService) {
        this.blobService = blobService;
    }

    @PostMapping
    public ContentBlob uploadBlob(@RequestBody BlobUploadRequest request) {
        return blobService.saveBlob(request);
    }

    @GetMapping("/{id}")
    public ContentBlob getBlob(@PathVariable String id) {
        return blobService.getBlob(id);
    }
}