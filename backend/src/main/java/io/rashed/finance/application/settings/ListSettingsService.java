package io.rashed.finance.application.settings;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.settings.Setting;
import io.rashed.finance.domain.settings.SettingsRepository;

@Service
public class ListSettingsService {

    private final SettingsRepository repository;

    public ListSettingsService(SettingsRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Setting> execute() {
        return repository.findAll();
    }
}
