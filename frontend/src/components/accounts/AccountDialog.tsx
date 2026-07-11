import type { ReactNode } from "react";

interface Props {

    open: boolean;

    title: string;

    children: ReactNode;

    onClose: () => void;
}

export default function AccountDialog({

    open,
    title,
    children,
    onClose

}: Props) {

    if (!open) {
        return null;
    }

    return (

        <div
            style={{
                position: "fixed",
                inset: 0,
                background: "rgba(0,0,0,.4)",

                display: "flex",
                justifyContent: "center",
                alignItems: "center"
            }}
        >

            <div
                style={{
                    background: "white",
                    width: 500,
                    padding: 24,
                    borderRadius: 8
                }}
            >

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between"
                    }}
                >

                    <h2>{title}</h2>

                    <button
                        onClick={onClose}
                    >
                        ✕
                    </button>

                </div>

                {children}

            </div>

        </div>

    );

}