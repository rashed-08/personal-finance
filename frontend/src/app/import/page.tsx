import GoogleKeepImportForm from "../../components/migration/GoogleKeepImportForm";

export default function ImportPage() {
    return (
        <>
            <div className="page-header">
                <div>
                    <h1 className="page-header__title">Import</h1>
                    <p className="page-header__subtitle">
                        Bring historical financial data into the app. Currently supports Google Keep notes —
                        CSV import is coming later.
                    </p>
                </div>
            </div>

            <div className="dashboard-section">
                <h2 className="dashboard-section__title">Google Keep Migration</h2>
                <GoogleKeepImportForm />
            </div>
        </>
    );
}
