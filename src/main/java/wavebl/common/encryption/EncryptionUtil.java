package wavebl.common.encryption;


//my mock cryptography is that public key must have first symbols the same as last symbol in private key
public class EncryptionUtil {

    public static String encodeWithPrivateKey(String toEncode, String privateKey) {
        return privateKey.charAt(privateKey.length() - 1) + toEncode;
    }

    public static String encodeWithPublicKey(String toEncode, String publicKey) {
        return publicKey.charAt(0) + toEncode;
    }

    public static boolean verify(String generated, String signature, String publicKey) {
        generated = publicKey.charAt(0) + generated;
        return generated.equals(signature);
    }

    public static String decode(String signature, String privateKey) {
        return signature.charAt(0) == privateKey.charAt(privateKey.length() - 1) ?
                signature.substring(1) : "aa";
    }
}
