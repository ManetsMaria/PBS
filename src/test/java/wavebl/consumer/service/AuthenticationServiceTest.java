package wavebl.consumer.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wavebl.common.dto.ActionAuthRequest;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.SignedNonce;
import wavebl.common.encryption.EncryptionUtil;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.consumer.repository.InMemoryKeyStorage;

public class AuthenticationServiceTest {
    AuthenticationService authenticationService;
    InMemoryKeyStorage inMemoryKeyStorage;

    @BeforeEach
    public void init() {
        inMemoryKeyStorage = new InMemoryKeyStorage("Aa", "tA", "YU");
        authenticationService = new AuthenticationService(inMemoryKeyStorage);
    }

    @Test
    public void simpleValidateAndGetNonceTest() {
        long time = System.currentTimeMillis();
        String nonce = "12345";
        ActionType actionType = ActionType.GET_SHADOW;
        int bucketId = 1;
        String toEncode = TemplateGenerationUtil.authenticationResponse(actionType, bucketId, time, nonce);
        String signature = EncryptionUtil.encodeWithPublicKey(toEncode, inMemoryKeyStorage.myPublicKey());
        SignedNonce signedNonce = new SignedNonce(time, new ActionAuthRequest(bucketId, actionType, "", "", 2), signature);
        String extractNonce = authenticationService.validateAndGetNonce(signedNonce);
        Assertions.assertEquals(nonce, extractNonce);
    }

    @Test
    public void validateAndGetExpiredNonceTest() throws InterruptedException {
        long time = System.currentTimeMillis();
        Thread.sleep(2000);
        String nonce = "12345";
        ActionType actionType = ActionType.GET_SHADOW;
        int bucketId = 1;
        String toEncode = TemplateGenerationUtil.authenticationResponse(actionType, bucketId, time, nonce);
        String signature = EncryptionUtil.encodeWithPublicKey(toEncode, inMemoryKeyStorage.myPublicKey());
        SignedNonce signedNonce = new SignedNonce(time, new ActionAuthRequest(bucketId, actionType, "", "", 2), signature);
        String extractNonce = authenticationService.validateAndGetNonce(signedNonce);
        Assertions.assertNull(extractNonce);
    }

    @Test
    public void validateAndGetSpeculatedSignatureTest() {
        long time = System.currentTimeMillis();
        String nonce = "12345";
        ActionType actionType = ActionType.GET_SHADOW;
        int bucketId = 1;
        String toEncode = TemplateGenerationUtil.authenticationResponse(actionType, bucketId, time, nonce);
        String signature = EncryptionUtil.encodeWithPublicKey(toEncode, "45");
        SignedNonce signedNonce = new SignedNonce(time, new ActionAuthRequest(bucketId, actionType, "", "", 2), signature);
        String extractNonce = authenticationService.validateAndGetNonce(signedNonce);
        Assertions.assertNull(extractNonce);
    }
}
