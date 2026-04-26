package sync;

import java.util.Map;

/**
 * PIPE-06 Fix [failure mode]: ConflictPair stores the decrypted state of both
 * competing versions to allow the UI to perform a merge or selection.
 */
public record ConflictPair(
    Map<String, Object> localFields,   // decrypted fields from the client's version
    Map<String, Object> serverFields,  // decrypted fields from the server's current version
    int serverVersionSeq               // the version seq to use when posting the resolution
) {}
