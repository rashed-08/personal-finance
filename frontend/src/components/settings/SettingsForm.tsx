import { useMemo, useState } from "react";
import type { FormEvent } from "react";

import { useUpdateSettings } from "../../hooks/useSettings";
import { errorMessage } from "../../lib/errorMessage";
import { groupSettings } from "../../lib/settingGroups";
import type { Setting } from "../../types/settings";

import SettingField from "./SettingField";

interface Props {
    settings: Setting[];
}

type Draft = Record<string, string | null>;

function toDraft(settings: Setting[]): Draft {
    return Object.fromEntries(settings.map((setting) => [setting.key, setting.value]));
}

export default function SettingsForm({ settings }: Props) {
    const mutation = useUpdateSettings();

    // Keyed on the saved values so that a save (which refetches) and an
    // external change both reset the editor, without an effect that
    // writes state during render.
    const saved = useMemo(() => toDraft(settings), [settings]);

    const [draft, setDraft] = useState<Draft>(saved);
    const [editing, setEditing] = useState(saved);

    if (editing !== saved) {
        setEditing(saved);
        setDraft(saved);
    }

    const groups = useMemo(() => groupSettings(settings), [settings]);

    /** Only what actually changed is sent; the batch is applied atomically. */
    const changed = useMemo(
        () =>
            Object.fromEntries(
                Object.entries(draft).filter(([key, value]) => value !== saved[key]),
            ),
        [draft, saved],
    );

    const changedCount = Object.keys(changed).length;

    function handleChange(key: string, value: string | null) {
        setDraft((current) => ({ ...current, [key]: value }));
    }

    function handleSubmit(e: FormEvent) {
        e.preventDefault();

        if (changedCount === 0) {
            return;
        }

        mutation.mutate({ values: changed });
    }

    function handleReset() {
        setDraft(saved);
        mutation.reset();
    }

    return (
        <form className="form" onSubmit={handleSubmit}>
            {groups.map((group) => (
                <div className="dashboard-section" key={group.title}>
                    <h2 className="dashboard-section__title">{group.title}</h2>
                    <p className="page-header__subtitle">{group.description}</p>

                    <div className="card" style={{ padding: 20 }}>
                        {group.settings.map((setting) => (
                            <SettingField
                                key={setting.key}
                                setting={setting}
                                value={draft[setting.key] ?? null}
                                onChange={handleChange}
                            />
                        ))}
                    </div>
                </div>
            ))}

            {mutation.isError && (
                <div className="form-error" role="alert">
                    <span>⚠</span>
                    <span>{errorMessage(mutation.error)}</span>
                </div>
            )}

            {mutation.isSuccess && changedCount === 0 && (
                <div className="form-warning" role="status">
                    <span>✓</span>
                    <span>Settings saved.</span>
                </div>
            )}

            <div className="form-actions">
                <button
                    type="button"
                    className="btn btn--ghost"
                    onClick={handleReset}
                    disabled={changedCount === 0 || mutation.isPending}
                >
                    Discard changes
                </button>

                <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={changedCount === 0 || mutation.isPending}
                >
                    {mutation.isPending
                        ? "Saving…"
                        : changedCount === 0
                          ? "Save changes"
                          : `Save ${changedCount} change${changedCount === 1 ? "" : "s"}`}
                </button>
            </div>
        </form>
    );
}
