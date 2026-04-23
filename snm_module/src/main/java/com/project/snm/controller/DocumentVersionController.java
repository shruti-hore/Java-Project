package com.project.snm.controller;

import com.project.snm.dto.CreateDocumentVersionRequest;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.service.DocumentVersionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping("/{uuid}/versions")
    public DocumentVersion createVersion(
            @PathVariable String uuid,
            @RequestBody CreateDocumentVersionRequest request
    ) {
        return documentVersionService.createVersion(uuid, request);
    }

    @GetMapping("/{uuid}/versions")
    public List<DocumentVersion> getVersions(@PathVariable String uuid) {
        return documentVersionService.getVersions(uuid);
    }
}