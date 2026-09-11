package io.rashed.finance.infrastructure.backup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.rashed.finance.application.backup.BackupFailedException;

/**
 * Runs {@code pg_dump} / {@code pg_restore} as a child process.
 *
 * The password goes in the environment rather than on the command line,
 * where it would be visible to anything that can list processes.
 */
@Component
public class PostgresToolRunner {

    private static final Logger log = LoggerFactory.getLogger(PostgresToolRunner.class);

    /**
     * Cap on captured output. These tools are quiet on success and produce
     * a few lines on failure; a runaway stream must not be buffered whole.
     */
    private static final int MAX_OUTPUT_CHARS = 8_000;

    /**
     * @param workingDirectory directory to run in, or null for the
     *                         application's own.
     * @throws BackupFailedException if the tool is missing, times out, or
     *                               exits non-zero.
     */
    public void run(
            List<String> command,
            PostgresConnection connection,
            Duration timeout,
            Path workingDirectory
    ) {

        Objects.requireNonNull(command, "Command cannot be null.");
        Objects.requireNonNull(connection, "Connection cannot be null.");

        ProcessBuilder builder = new ProcessBuilder(command);

        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }

        if (connection.password() != null) {
            builder.environment().put("PGPASSWORD", connection.password());
        }

        // stderr into stdout: these tools write progress and errors to
        // stderr, and one interleaved stream is what we want to report.
        builder.redirectErrorStream(true);

        Process process = start(builder, command);

        try {
            String output = readOutput(process.getInputStream());

            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {

                process.destroyForcibly();

                throw new BackupFailedException(
                        command.getFirst() + " did not finish within " + timeout + ".");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new BackupFailedException(
                        command.getFirst() + " failed with exit code " + exitCode
                                + (output.isBlank() ? "." : ": " + output));
            }

            if (!output.isBlank()) {
                log.info("{} output: {}", command.getFirst(), output);
            }

        } catch (InterruptedException ex) {

            process.destroyForcibly();

            // Restore the flag so whatever is shutting the thread down
            // still sees the interrupt.
            Thread.currentThread().interrupt();

            throw new BackupFailedException(command.getFirst() + " was interrupted.", ex);

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not read output from " + command.getFirst() + ".", ex);
        }
    }

    private Process start(ProcessBuilder builder, List<String> command) {

        try {
            return builder.start();

        } catch (IOException ex) {
            throw new BackupFailedException(
                    "Could not run " + command.getFirst()
                            + ". Install the PostgreSQL client tools and put them on PATH, "
                            + "or set app.backup.postgres.pg-dump / pg-restore to their full paths. "
                            + "The JSON backup format needs no external tools.",
                    ex
            );
        }
    }

    /**
     * Drains the process output as it is produced. Reading before
     * {@code waitFor} matters: a process whose output pipe fills up blocks
     * forever, and waiting first would deadlock against it.
     */
    private String readOutput(InputStream stream) throws IOException {

        byte[] bytes = stream.readNBytes(MAX_OUTPUT_CHARS);

        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
