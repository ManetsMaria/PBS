package wavebl.pbs.encryption;

import java.security.SecureRandom;

public class NonceGenerationUtil {

    private static final String AVAILABLE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MINIMUM_SIZE = 5;
    private static final int MAXIMUM_SIZE = 25;

    public static String generateNonce() {
        SecureRandom random = new SecureRandom();
        StringBuilder nonce = new StringBuilder();
        int length = MINIMUM_SIZE + random.nextInt(MAXIMUM_SIZE - MINIMUM_SIZE + 1);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(AVAILABLE_CHARACTERS.length());
            nonce.append(AVAILABLE_CHARACTERS.charAt(index));
        }
        return nonce.toString();
    }
}
