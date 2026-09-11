package io.rashed.finance.common.enums;

/**
 * Declared type of a setting's stored value.
 *
 * Settings live in a key-value table, so every value is persisted as text.
 * This tells the reader how to interpret it and lets writes be rejected
 * before they reach the database.
 */
public enum SettingValueType {

    STRING,

    INTEGER,

    DECIMAL,

    BOOLEAN,

    DATE,

    JSON

}
