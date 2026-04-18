import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;

public class Argon2Service {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}