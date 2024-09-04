package wavebl.pbs.repository.nonce_cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// I didn't add any check for cache size and memory because trust guava
public class NonceCacheTest {

    NonceCache nonceCache;

    @BeforeEach
    public void init() {
        nonceCache = new NonceCache();
    }

    @Test
    public void simpleValidFlowTest() {
        nonceCache.addNonce("1", 1, 1);
        Assertions.assertTrue(nonceCache.validateAndRemove("1", 1, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void expiredNonceTest() throws InterruptedException {
        nonceCache.addNonce("1", 1, 1);
        Thread.sleep(2000);
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void waitButNotExpiredNonceTest() throws InterruptedException {
        nonceCache.addNonce("1", 1, 1);
        Thread.sleep(1500);
        Assertions.assertTrue(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void spoiledNonceTest() {
        nonceCache.addNonce("1", 1, 1);
        nonceCache.removeNonce("1");
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void unknownNonceFalseTest() {
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void unknownBucketFalseTest() {
        nonceCache.addNonce("1", 1, 1);
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 2, 1));
    }

    @Test
    public void spoiledNonceAfterIncorrectBucketTest() {
        nonceCache.addNonce("1", 1, 1);
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 2, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }

    @Test
    public void nullAbsentNotThrowingExceptionsTest() {
        nonceCache.addNonce(null, 1, 1);
        nonceCache.addNonce(null, null, null);
        nonceCache.addNonce("1", null, 1);
        nonceCache.removeNonce("5");
        nonceCache.removeNonce(null);
        nonceCache.validateAndRemove(null, null, null);
        nonceCache.validateAndRemove("1", null, 1);
        nonceCache.validateAndRemove(null, 1, null);
    }

    @Test
    public void severalNonceStorageTest() throws InterruptedException {
        nonceCache.addNonce("1", 1, 1);
        nonceCache.addNonce("2", 2, 1);
        Thread.sleep(1000);
        nonceCache.addNonce("3", 1, 1);
        Assertions.assertTrue(nonceCache.validateAndRemove("1", 1, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove("4", 4, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove(null, 1, 1));
        Thread.sleep(1000);
        Assertions.assertTrue(nonceCache.validateAndRemove("3", 1, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove("2", 2, 1));

        nonceCache.addNonce("1", 1, 1);
        nonceCache.addNonce("2", 2, 1);
        nonceCache.addNonce("3", 1, 1);
        Assertions.assertTrue(nonceCache.validateAndRemove("3", 1, 1));
        nonceCache.removeNonce("2");
        nonceCache.removeNonce(null);
        nonceCache.removeNonce("5");
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 2));
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 2, 1));
        Assertions.assertFalse(nonceCache.validateAndRemove("1", 1, 1));
    }
}
