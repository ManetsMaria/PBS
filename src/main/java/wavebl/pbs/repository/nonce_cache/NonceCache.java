package wavebl.pbs.repository.nonce_cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import wavebl.pbs.entity.BucketTimestampClientTriple;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static wavebl.pbs.service.AuthenticationService.NONCE_VALID_TIME_IN_SECONDS;

public class NonceCache {

    //We need clientId for cases when nonce is used by another "registered" client with their valid certificate/public key pair
    private final Cache <String, BucketTimestampClientTriple> cache;

    public NonceCache() {
        //make one more second for live to make sure that it will be not removed too early
        cache = CacheBuilder.newBuilder().expireAfterWrite(NONCE_VALID_TIME_IN_SECONDS + 1, TimeUnit.SECONDS).build();
    }

    public Long addNonce(String nonce, Integer bucketId, Integer clientId) {
        if (nonce == null || bucketId == null) {
            return null;
        }
        long currentTime = System.currentTimeMillis();
        BucketTimestampClientTriple bucketTimestampClientTriple = new BucketTimestampClientTriple(bucketId, currentTime, clientId);
        //it's not zero probability that generated nonce for several clients/actions will be the same but low enough to ignore this case
        cache.put(nonce, bucketTimestampClientTriple);
        return currentTime;
    }

    public boolean validateAndRemove(String nonce, Integer bucketId, Integer clientId) {
        if (nonce == null) {
            return false;
        }
        AtomicBoolean isValid = new AtomicBoolean(false);
        long currentTime = System.currentTimeMillis();
        //remove nonce in any case because it's already spoiled
        cache.asMap().computeIfPresent(nonce, (k, v) -> {
            isValid.set(bucketId.equals(v.bucketId()) && currentTime < (v.timestamp() + NONCE_VALID_TIME_IN_SECONDS * 1000L) && clientId.equals(v.clientId()));
            return null;
        });
        return isValid.get();
    }

    public void removeNonce(String nonce) {
        if (nonce != null) {
            //if nonce absent guava cache throw nothing
            cache.invalidate(nonce);
        }
    }
}
