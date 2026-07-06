package com.example.bhojhon.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted, iterated password hashing using PBKDF2 (HMAC-SHA256).
 *
 * <p>Replaces the previous {@code String.hashCode()} scheme, which was a 32-bit
 * unsalted hash — effectively plaintext. All primitives here are part of
 * {@code java.base}, so no extra dependency or module requirement is needed.
 *
 * <p>Stored format: {@code pbkdf2$<iterations>$<saltBase64>$<hashBase64>}.
 * {@link #verify(String, String)} also accepts values in the old
 * {@code hashCode()} format so existing rows keep working until the user next
 * logs in (at which point the caller may transparently re-hash — see
 * {@link #isLegacy(String)}).
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final String PREFIX = "pbkdf2";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Produces a salted PBKDF2 hash string for the given plaintext password. */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$" + encode(salt) + "$" + encode(derived);
    }

    /**
     * Verifies a plaintext password against a stored hash. Supports both the
     * current PBKDF2 format and the legacy {@code hashCode()} format.
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        if (stored.startsWith(PREFIX + "$")) {
            String[] parts = stored.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = decode(parts[2]);
                byte[] expected = decode(parts[3]);
                byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);
                return MessageDigest.isEqual(expected, actual);
            } catch (RuntimeException e) {
                return false;
            }
        }
        // Legacy: previous scheme stored Integer.toString(password.hashCode()).
        return stored.equals(Integer.toString(password.hashCode()));
    }

    /** True if the stored value is in the old (insecure) format and should be upgraded. */
    public static boolean isLegacy(String stored) {
        return stored != null && !stored.startsWith(PREFIX + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 hashing unavailable", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }
}
