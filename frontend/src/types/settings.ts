export type SettingValueType =
    | "STRING"
    | "INTEGER"
    | "DECIMAL"
    | "BOOLEAN"
    | "DATE"
    | "JSON";

export interface Setting {
    /** Stable identifier, e.g. "DEFAULT_CURRENCY". */
    key: string;

    /** Stored value as text. Null means unset — the backend falls back to its own default. */
    value: string | null;

    /** How to interpret `value`; decides which form control is rendered. */
    valueType: SettingValueType;

    description: string | null;

    /** Seeded by migration and understood by code: editable, never deletable. */
    system: boolean;

    updatedAt: string;
}

export interface UpdateSettingsRequest {
    /** Key to new value. Null unsets the setting. Applied all-or-nothing. */
    values: Record<string, string | null>;
}
