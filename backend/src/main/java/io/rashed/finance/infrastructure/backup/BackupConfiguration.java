package io.rashed.finance.infrastructure.backup;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wiring for the backup module.
 *
 * Scheduling is enabled here rather than on the application class because
 * the automatic backup is the only scheduled job in the system — see
 * {@code RunScheduledBackupService}.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(PostgresToolProperties.class)
public class BackupConfiguration {
}
