package one.atticus.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class PasswordUtil {
    private static final Logger log = LoggerFactory.getLogger(PasswordUtil.class);

    public static byte[] passwordHash(String password) {
        try {
            // generate 32 bytes of salt
            byte[] salt = new byte[32];
            SecureRandom.getInstanceStrong().nextBytes(salt);

            // create password hash
            byte[] pbkdf2 = pbkdf2(password.toCharArray(), salt);

            // pack salt and hash together
            byte[] hash = new byte[64];
            System.arraycopy(salt, 0, hash, 0, salt.length);
            System.arraycopy(pbkdf2, salt.length, hash, 0, pbkdf2.length);
            return hash;
        }
        catch(NoSuchAlgorithmException e) {
            log.error("crypto config problem", e);
            throw new RuntimeException(e);
        }

    }

    public static boolean verifyPasswordHash(byte[] hash, String password) {
        // provided hash length must be 64
        if(hash.length != 64) {
            return false;
        }

        // extract 32 bytes of salt
        byte[] salt = new byte[32];
        System.arraycopy(hash, 0, salt, 0, salt.length);

        // create password hash with defined salt
        byte[] pbkdf2 = pbkdf2(password.toCharArray(), salt);

        // verify the remaining 32 bytes (password hash) match
        for(int i = 0; i < hash.length - salt.length; i++) {
            if(hash[i + salt.length] != pbkdf2[i]) {
                return false;
            }
        }

        // all verifications passed
        return true;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec key = new PBEKeySpec(password, salt, 1024, 256); // iteration count, key length
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(key).getEncoded();
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("crypto config problem", e);
            throw new RuntimeException(e);
        }
    }
}
