// CSM.java
public class CSM {

    private AESGCM aes;
    private RSA rsa;
    private PBKDF2 pbkdf2;

    public CSM() {
        aes = new AESGCM();
        rsa = new RSA();
        pbkdf2 = new PBKDF2();
    }

    public AESGCM getAES() {
        return aes;
    }

    public RSA getRSA() {
        return rsa;
    }

    public PBKDF2 getPBKDF2() {
        return pbkdf2;
    }
}