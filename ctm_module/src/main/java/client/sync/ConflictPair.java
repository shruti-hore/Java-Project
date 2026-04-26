package client.sync;

/**
 * Stub: Represents a conflict between local and remote versions of a document.
 */
public class ConflictPair {
    private String documentUuid;
    private String localVersion;
    private String remoteVersion;

    public ConflictPair() {}

    public ConflictPair(String documentUuid, String localVersion, String remoteVersion) {
        this.documentUuid = documentUuid;
        this.localVersion = localVersion;
        this.remoteVersion = remoteVersion;
    }

    public String getDocumentUuid() { return documentUuid; }
    public String getLocalVersion() { return localVersion; }
    public String getRemoteVersion() { return remoteVersion; }
}
