package com.project.snm.controller;

import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.service.SyncService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/{documentUuid}/latest")
    public DocumentVersion getLatestVersion(@PathVariable String documentUuid) {
        return syncService.getLatestVersion(documentUuid);
    }

    @GetMapping("/{documentUuid}/all")
    public List<DocumentVersion> getAllVersions(@PathVariable String documentUuid) {
        return syncService.getAllVersions(documentUuid);
    }
}