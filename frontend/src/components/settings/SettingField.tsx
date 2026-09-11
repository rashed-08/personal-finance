import type { Setting } from "../../types/settings";

import { needsRestart, settingChoices, settingLabel } from "../../lib/settingGroups";

interface Props {
    setting: Setting;

    /** Current editor value, which may differ from the saved one. */
    value: string | null;

    onChange(key: string, value: string | null): void;
}

/**
 * One setting, rendered as the control its declared type calls for.
 *
 * The backend validates every value against that type anyway, so the
 * point of matching the control is to make an invalid value hard to
 * produce rather than to be the only line of defence.
 */
export default function SettingField({ setting, value, onChange }: Props) {
    const inputId = `setting-${setting.key}`;
    const label = settingLabel(setting.key);
    const choices = settingChoices(setting.key);

    const hint = [
        setting.description,
        needsRestart(setting.key) ? "Takes effect after the server restarts." : null,
    ]
        .filter(Boolean)
        .join(" ");

    if (setting.valueType === "BOOLEAN") {
        return (
            <div className="field">
                <label className="field-checkbox" htmlFor={inputId}>
                    <input
                        id={inputId}
                        type="checkbox"
                        checked={value === "true"}
                        onChange={(e) => onChange(setting.key, String(e.target.checked))}
                    />
                    <span>{label}</span>
                </label>

                {hint && <span className="field__hint">{hint}</span>}
            </div>
        );
    }

    return (
        <div className="field">
            <label className="field__label" htmlFor={inputId}>
                {label}
            </label>

            {choices ? (
                <select
                    id={inputId}
                    className="select"
                    value={value ?? ""}
                    onChange={(e) => onChange(setting.key, e.target.value)}
                >
                    {/* Present only when the stored value is not one of the
                        known choices, so an unexpected value is visible
                        rather than silently rewritten on save. */}
                    {value != null && !choices.includes(value) && (
                        <option value={value}>{value} (unrecognised)</option>
                    )}

                    {choices.map((choice) => (
                        <option key={choice} value={choice}>
                            {choice}
                        </option>
                    ))}
                </select>
            ) : (
                <input
                    id={inputId}
                    className="input"
                    type={setting.valueType === "INTEGER" ? "number" : "text"}
                    inputMode={setting.valueType === "INTEGER" ? "numeric" : undefined}
                    value={value ?? ""}
                    onChange={(e) =>
                        // Empty means "unset", which the backend represents
                        // as null, not "".
                        onChange(setting.key, e.target.value === "" ? null : e.target.value)
                    }
                />
            )}

            {hint && <span className="field__hint">{hint}</span>}
        </div>
    );
}
