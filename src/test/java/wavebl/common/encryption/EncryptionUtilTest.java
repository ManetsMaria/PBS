package wavebl.common.encryption;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

//do not really cover by tests, just to make sure that there is no logical errors that can influence on main part
public class EncryptionUtilTest {

    //A - is common symbol (last in private and first in public)
    final String validPairPrivateKey = "hjdfgdjA";
    final String validPairPublicKey = "Agjfuyijk";

    final String invalidPairPrivateKey = "ghyuj";
    final String invalidPairPublicKey = "hjkfe";


    @Test
    public void encodeWithPrivateKeyTest() {
        String str = "12";
        String result = EncryptionUtil.encodeWithPrivateKey(str, validPairPrivateKey);
        assertEquals("A12", result);
    }

    @Test
    public void encodeWithPublicKeyTest() {
        String str = "12";
        String result = EncryptionUtil.encodeWithPublicKey(str, validPairPublicKey);
        assertEquals("A12", result);
    }

    @Test
    public void verifyTrueTest() {
        String str = "12";
        String encoded = EncryptionUtil.encodeWithPrivateKey(str, validPairPrivateKey);
        Assertions.assertTrue(EncryptionUtil.verify(str, encoded, validPairPublicKey));
    }

    @Test
    public void verifyFalseTest() {
        String str = "12";
        String encoded = EncryptionUtil.encodeWithPrivateKey(str, validPairPrivateKey);
        Assertions.assertFalse(EncryptionUtil.verify("23", encoded, validPairPublicKey));
    }

    @Test
    public void decodeValidPairTest() {
        String str = "12";
        String encoded = EncryptionUtil.encodeWithPublicKey(str, validPairPublicKey);
        String decoded = EncryptionUtil.decode(encoded, validPairPrivateKey);
        assertEquals(str, decoded);
    }

    @Test
    public void decodeInvalidPairTest() {
        String str = "12";
        String encoded = EncryptionUtil.encodeWithPublicKey(str, invalidPairPublicKey);
        String decoded = EncryptionUtil.decode(encoded, invalidPairPrivateKey);
        Assertions.assertNotEquals(str, decoded);
    }
}
