package io.rashed.finance.application.backup;

/**
 * What the frontend needs to know about the Google Drive link.
 *
 * @param configured   whether the server has OAuth credentials at all. False
 *                     means Drive cannot be offered until the deployment is
 *                     configured — not something the user can fix.
 * @param connected    whether a usable Drive grant is stored.
 * @param accountEmail Google account the grant belongs to, when connected.
 * @param folderName   Drive folder archives are written to.
 * @param detail       why Drive is unusable, when it is. Null otherwise.
 */
public record GoogleDriveConnection(
        boolean configured,
        boolean connected,
        String accountEmail,
        String folderName,
        String detail
) {

    public static GoogleDriveConnection notConfigured(String detail) {
        return new GoogleDriveConnection(false, false, null, null, detail);
    }

    public static GoogleDriveConnection disconnected(String folderName) {
        return new GoogleDriveConnection(true, false, null, folderName, "Google Drive is not connected.");
    }

    public static GoogleDriveConnection connected(String accountEmail, String folderName) {
        return new GoogleDriveConnection(true, true, accountEmail, folderName, null);
    }
}
