package io.rashed.finance.common.enums;

/**
 * Archive format, recorded per operation rather than read from settings:
 * a restore must use the reader that matches the archive it was given,
 * and the configured default may have changed since it was written.
 */
public enum BackupFormat {

    /**
     * Application-level export: every table serialized to a versioned JSON
     * document inside a zip. Portable, needs no external tooling.
     */
    JSON,

    /**
     * Physical {@code pg_dump} custom-format dump. Authoritative, but
     * requires the Postgres client tools on the host and a server version
     * the local {@code pg_restore} understands.
     */
    PG_DUMP;

    /**
     * Extension used for archives in this format, without the leading dot.
     */
    public String extension() {

        return switch (this) {
            case JSON -> "zip";
            case PG_DUMP -> "dump";
        };
    }
}
