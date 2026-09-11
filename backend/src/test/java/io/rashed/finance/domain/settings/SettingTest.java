package io.rashed.finance.domain.settings;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.rashed.finance.common.enums.BackupFormat;
import io.rashed.finance.common.enums.SettingValueType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingTest {

    private static Setting setting(String value, SettingValueType type) {
        return Setting.create("A_KEY", value, type, null);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void create_rejectsBlankKey() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Setting.create("  ", "x", SettingValueType.STRING, null)
        );
    }

    @Test
    void create_trimsKeyAndValue() {

        Setting setting = Setting.create("  A_KEY  ", "  BDT  ", SettingValueType.STRING, null);

        assertEquals("A_KEY", setting.getKey());
        assertEquals("BDT", setting.getValue());
    }

    @Test
    void create_rejectsValueThatDoesNotMatchDeclaredType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> setting("not-a-number", SettingValueType.INTEGER)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> setting("yes", SettingValueType.BOOLEAN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> setting("31-12-2026", SettingValueType.DATE)
        );
    }

    @Test
    void create_storesJsonVerbatim() {

        // The shape belongs to whichever module defined the key, so this
        // aggregate must not try to validate it.
        Setting setting = setting("{ not strictly json ", SettingValueType.JSON);

        assertEquals("{ not strictly json", setting.getValue());
    }

    // -------------------------------------------------------------------------
    // Unset values
    // -------------------------------------------------------------------------

    @Test
    void blankValueIsStoredAsUnset() {

        assertNull(setting("   ", SettingValueType.STRING).getValue());
        assertFalse(setting("", SettingValueType.STRING).hasValue());
        assertFalse(setting(null, SettingValueType.INTEGER).hasValue());
    }

    @Test
    void unsetValueFallsBackToCallerDefault() {

        assertEquals("BDT", setting(null, SettingValueType.STRING).asString("BDT"));
        assertTrue(setting(null, SettingValueType.BOOLEAN).asBoolean(true));
        assertEquals(30, setting(null, SettingValueType.INTEGER).asInt(30));
        assertEquals(
                new BigDecimal("1.50"),
                setting(null, SettingValueType.DECIMAL).asDecimal(new BigDecimal("1.50"))
        );
        assertEquals(
                LocalDate.of(2026, 1, 1),
                setting(null, SettingValueType.DATE).asDate(LocalDate.of(2026, 1, 1))
        );
    }

    // -------------------------------------------------------------------------
    // Typed readers
    // -------------------------------------------------------------------------

    @Test
    void readsEachDeclaredType() {

        assertTrue(setting("true", SettingValueType.BOOLEAN).asBoolean(false));
        assertFalse(setting("FALSE", SettingValueType.BOOLEAN).asBoolean(true));
        assertEquals(7, setting("7", SettingValueType.INTEGER).asInt(0));
        assertEquals(7L, setting("7", SettingValueType.INTEGER).asLong(0));
        assertEquals(
                new BigDecimal("2.25"),
                setting("2.25", SettingValueType.DECIMAL).asDecimal(BigDecimal.ZERO)
        );
        assertEquals(
                LocalDate.of(2026, 7, 25),
                setting("2026-07-25", SettingValueType.DATE).asDate(null)
        );
    }

    @Test
    void readingAsWrongTypeIsAProgrammingError() {

        Setting text = setting("BDT", SettingValueType.STRING);

        assertThrows(IllegalStateException.class, () -> text.asBoolean(false));
        assertThrows(IllegalStateException.class, () -> text.asInt(0));
    }

    @Test
    void asEnum_isCaseInsensitive() {

        assertEquals(
                BackupFormat.PG_DUMP,
                setting("pg_dump", SettingValueType.STRING)
                        .asEnum(BackupFormat.class, BackupFormat.JSON)
        );
    }

    @Test
    void asEnum_fallsBackWhenTheStoredConstantIsUnknown() {

        // A value left behind by an older version must not stop the
        // application booting.
        assertEquals(
                BackupFormat.JSON,
                setting("PARQUET", SettingValueType.STRING)
                        .asEnum(BackupFormat.class, BackupFormat.JSON)
        );
    }

    // -------------------------------------------------------------------------
    // Updating
    // -------------------------------------------------------------------------

    @Test
    void changeValue_validatesAgainstTheDeclaredType() {

        Setting count = setting("10", SettingValueType.INTEGER);

        assertEquals(20, count.changeValue("20").asInt(0));
        assertThrows(IllegalArgumentException.class, () -> count.changeValue("twenty"));
    }

    @Test
    void changeValue_keepsIdentityAndType() {

        Setting original = setting("10", SettingValueType.INTEGER);
        Setting updated = original.changeValue("20");

        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getKey(), updated.getKey());
        assertEquals(SettingValueType.INTEGER, updated.getValueType());
    }

    @Test
    void changeValue_canUnsetASetting() {

        assertFalse(setting("10", SettingValueType.INTEGER).changeValue(null).hasValue());
    }
}
