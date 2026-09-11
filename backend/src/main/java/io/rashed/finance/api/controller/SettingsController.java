package io.rashed.finance.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.rashed.finance.api.dto.settings.SettingResponse;
import io.rashed.finance.api.dto.settings.SettingsDtoMapper;
import io.rashed.finance.api.dto.settings.UpdateSettingsRequest;
import io.rashed.finance.application.settings.ListSettingsService;
import io.rashed.finance.application.settings.UpdateSettingsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ListSettingsService listSettingsService;
    private final UpdateSettingsService updateSettingsService;

    public SettingsController(
            ListSettingsService listSettingsService,
            UpdateSettingsService updateSettingsService
    ) {
        this.listSettingsService = listSettingsService;
        this.updateSettingsService = updateSettingsService;
    }

    @GetMapping
    public List<SettingResponse> list() {

        return listSettingsService.execute()
                .stream()
                .map(SettingsDtoMapper::toResponse)
                .toList();
    }

    /**
     * Applies a batch of changes and returns the settings that changed.
     */
    @PutMapping
    public List<SettingResponse> update(@Valid @RequestBody UpdateSettingsRequest request) {

        return updateSettingsService.execute(request.values())
                .stream()
                .map(SettingsDtoMapper::toResponse)
                .toList();
    }
}
