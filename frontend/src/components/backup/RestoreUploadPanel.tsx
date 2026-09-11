import { useRef, useState } from "react";

interface Props {
    onSelected(file: File): void;
}

/**
 * Picks an archive off disk to restore from.
 *
 * Selecting a file does not start anything — it opens the confirmation
 * dialog, which is where the restore is actually authorized.
 */
export default function RestoreUploadPanel({ onSelected }: Props) {
    const inputRef = useRef<HTMLInputElement>(null);
    const [fileName, setFileName] = useState<string | null>(null);

    return (
        <div className="card" style={{ padding: 20 }}>
            <div className="field">
                <label className="field__label" htmlFor="restore-file">
                    Restore from a file
                </label>

                <input
                    id="restore-file"
                    ref={inputRef}
                    type="file"
                    className="input"
                    accept=".zip,.dump"
                    onChange={(e) => {
                        const file = e.target.files?.[0];

                        if (file) {
                            setFileName(file.name);
                            onSelected(file);
                        }

                        // Clear the input so picking the same file again
                        // still fires a change event.
                        if (inputRef.current) {
                            inputRef.current.value = "";
                        }
                    }}
                />

                <span className="field__hint">
                    A <code>.zip</code> JSON export or a <code>.dump</code> PostgreSQL
                    dump produced by this application — for example one downloaded from
                    Google Drive by hand, or copied from another machine.
                    {fileName && <> Last selected: <code>{fileName}</code>.</>}
                </span>
            </div>
        </div>
    );
}
