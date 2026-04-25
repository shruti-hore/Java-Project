package client.dto;

import java.util.Map;

/**
 * PIPE-05 Fix [failure mode]: Client-side DTO for document version responses.
 * Replaced Lombok with a standard POJO for compatibility and simplicity.
 */
public class DocumentVersionResponse {
    private Long versionSeq;
    private String ciphertextBase64;
    private String nonceBase64;
    private String aadBase64;
    private Map<String, Integer> vectorClock;

    public DocumentVersionResponse() {}

    public DocumentVersionResponse(Long versionSeq, String ciphertextBase64, String nonceBase64, String aadBase64, Map<String, Integer> vectorClock) {
        this.versionSeq = versionSeq;
        this.ciphertextBase64 = ciphertextBase64;
        this.nonceBase64 = nonceBase64;
        this.aadBase64 = aadBase64;
        this.vectorClock = vectorClock;
    }

    public Long getVersionSeq() { return versionSeq; }
    public void setVersionSeq(Long versionSeq) { this.versionSeq = versionSeq; }
    public String getCiphertextBase64() { return ciphertextBase64; }
    public void setCiphertextBase64(String ciphertextBase64) { this.ciphertextBase64 = ciphertextBase64; }
    public String getNonceBase64() { return nonceBase64; }
    public void setNonceBase64(String nonceBase64) { this.nonceBase64 = nonceBase64; }
    public String getAadBase64() { return aadBase64; }
    public void setAadBase64(String aadBase64) { this.aadBase64 = aadBase64; }
    public Map<String, Integer> getVectorClock() { return vectorClock; }
    public void setVectorClock(Map<String, Integer> vectorClock) { this.vectorClock = vectorClock; }
}
