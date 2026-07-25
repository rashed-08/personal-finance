package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.migration.GoogleKeepMigrationResponse;
import io.rashed.finance.api.dto.migration.ImportGoogleKeepRequest;
import io.rashed.finance.api.dto.migration.MigrationDtoMapper;
import io.rashed.finance.application.migration.ImportGoogleKeepDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final ImportGoogleKeepDataService importGoogleKeepDataService;

    public MigrationController(ImportGoogleKeepDataService importGoogleKeepDataService) {
        this.importGoogleKeepDataService = importGoogleKeepDataService;
    }

    @PostMapping("/google-keep")
    public GoogleKeepMigrationResponse importGoogleKeep(@Valid @RequestBody ImportGoogleKeepRequest request) {

        return MigrationDtoMapper.toResponse(importGoogleKeepDataService.execute(request.content()));
    }
}
