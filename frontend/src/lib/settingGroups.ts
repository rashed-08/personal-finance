import type { Setting } from "../types/settings";

/**
 * Grouping and labelling for the settings screen.
 *
 * The backend returns a flat, alphabetical list of keys. Presenting it
 * that way would put backup options between date formats; this restores
 * the grouping the seed data intends, and gives each key a readable
 * label so the UI never shows raw SCREAMING_SNAKE_CASE.
 *
 * A key absent from here still renders — it falls into "Other" with a
 * label derived from the key — so adding a setting to the database does
 * not require touching this file to make it usable.
 */

export interface SettingGroup {
    title: string;
    description: string;
    keys: string[];
}

export const SETTING_GROUPS: SettingGroup[] = [
    {
        title: "General",
        description: "Currency, dates and regional preferences.",
        keys: ["DEFAULT_CURRENCY", "DATE_FORMAT", "FIRST_DAY_OF_WEEK"],
    },
    {
        title: "Salary Cycle",
        description:
            "How salary cycles are created and how leftover money moves between them.",
        keys: ["AUTO_ASSIGN_SALARY_CYCLE", "ENABLE_CARRY_FORWARD"],
    },
    {
        title: "Workflows",
        description: "Optional features that can be switched off entirely.",
        keys: ["ENABLE_CASH_RECONCILIATION", "ENABLE_RECURRING_TRANSACTIONS"],
    },
    {
        title: "Backup",
        description:
            "Where backups go and how many are kept. The schedule itself is set on the server.",
        keys: [
            "AUTO_BACKUP_ENABLED",
            "BACKUP_PROVIDER",
            "BACKUP_FORMAT",
            "BACKUP_DIRECTORY",
            "BACKUP_RETENTION_COUNT",
            "GOOGLE_DRIVE_FOLDER_NAME",
            "AUTO_BACKUP_CRON",
        ],
    },
];

const LABELS: Record<string, string> = {
    DEFAULT_CURRENCY: "Default currency",
    DATE_FORMAT: "Date format",
    FIRST_DAY_OF_WEEK: "First day of week",
    AUTO_ASSIGN_SALARY_CYCLE: "Assign transactions to a salary cycle automatically",
    ENABLE_CARRY_FORWARD: "Carry leftover money into the next cycle",
    ENABLE_CASH_RECONCILIATION: "Cash reconciliation",
    ENABLE_RECURRING_TRANSACTIONS: "Recurring transactions",
    AUTO_BACKUP_ENABLED: "Automatic backups",
    BACKUP_PROVIDER: "Backup destination",
    BACKUP_FORMAT: "Archive format",
    BACKUP_DIRECTORY: "Local backup directory",
    BACKUP_RETENTION_COUNT: "Automatic backups to keep",
    GOOGLE_DRIVE_FOLDER_NAME: "Google Drive folder",
    AUTO_BACKUP_CRON: "Backup schedule",
};

/**
 * Keys whose values are a fixed set. Rendered as a dropdown so a typo
 * cannot silently disable a feature — a value the backend does not
 * recognise falls back to its default rather than erroring.
 */
const CHOICES: Record<string, string[]> = {
    BACKUP_PROVIDER: ["LOCAL", "GOOGLE_DRIVE"],
    BACKUP_FORMAT: ["JSON", "PG_DUMP"],
    FIRST_DAY_OF_WEEK: [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY",
    ],
};

/**
 * Values the server reads only at startup, so a change here does not
 * take effect until the backend restarts. Flagged in the UI rather than
 * hidden: it is still the setting of record.
 */
const RESTART_REQUIRED = new Set(["AUTO_BACKUP_CRON"]);

export function settingLabel(key: string): string {
    return (
        LABELS[key] ??
        // "SOME_NEW_KEY" -> "Some new key"
        key
            .toLowerCase()
            .replace(/_/g, " ")
            .replace(/^./, (character) => character.toUpperCase())
    );
}

export function settingChoices(key: string): string[] | undefined {
    return CHOICES[key];
}

export function needsRestart(key: string): boolean {
    return RESTART_REQUIRED.has(key);
}

export interface GroupedSettings {
    title: string;
    description: string;
    settings: Setting[];
}

/**
 * Splits settings into the groups above, appending an "Other" group for
 * anything unrecognised so no key is ever invisible.
 */
export function groupSettings(settings: Setting[]): GroupedSettings[] {
    const byKey = new Map(settings.map((setting) => [setting.key, setting]));
    const grouped: GroupedSettings[] = [];
    const claimed = new Set<string>();

    for (const group of SETTING_GROUPS) {
        const members = group.keys
            .map((key) => byKey.get(key))
            .filter((setting): setting is Setting => setting !== undefined);

        members.forEach((setting) => claimed.add(setting.key));

        if (members.length > 0) {
            grouped.push({
                title: group.title,
                description: group.description,
                settings: members,
            });
        }
    }

    const rest = settings.filter((setting) => !claimed.has(setting.key));

    if (rest.length > 0) {
        grouped.push({
            title: "Other",
            description: "Settings not yet grouped on this screen.",
            settings: rest,
        });
    }

    return grouped;
}
