package auth.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class TokenService 
{
    public String generateToken(String email, byte[] authKey) 
    {
        try 
        {
            String payload = email + ":" + System.currentTimeMillis();

            byte[] signature = sign(payload.getBytes(), authKey);

            return Base64.getEncoder().encodeToString(payload.getBytes()) + "." + Base64.getEncoder().encodeToString(signature);
        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public boolean validateToken(String token, byte[] authKey) 
    {
        try 
        {
            String[] parts = token.split("\\.");

            byte[] payload = Base64.getDecoder().decode(parts[0]);
            byte[] signature = Base64.getDecoder().decode(parts[1]);

            byte[] expectedSig = sign(payload, authKey);

            return java.util.Arrays.equals(signature, expectedSig);

        } 
        catch (Exception e) 
        {
            return false;
        }
    }

    private byte[] sign(byte[] data, byte[] key) throws Exception 
    {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }
}
