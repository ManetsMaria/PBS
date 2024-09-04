package wavebl.pbs.service;

import wavebl.common.dto.ActionAuthRequest;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.SignedActionRequest;
import wavebl.common.dto.SignedNonce;
import wavebl.common.encryption.EncryptionUtil;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.pbs.encryption.NonceGenerationUtil;
import wavebl.pbs.repository.key.ClientPublicKeyStorage;
import wavebl.pbs.repository.key.PrivatePBSKeyStorage;
import wavebl.pbs.repository.nonce_cache.NonceCache;

public class AuthenticationService {

    //in real project it would be configured parameter but I just hardcoded
    public static int NONCE_VALID_TIME_IN_SECONDS = 2;

    private final ClientPublicKeyStorage clientPublicKeyStorage;
    private final NonceCache nonceCache;
    private final PrivatePBSKeyStorage privatePBSKeyStorage;

    public AuthenticationService(ClientPublicKeyStorage clientPublicKeyStorage, NonceCache nonceCache, PrivatePBSKeyStorage privatePBSKeyStorage) {
        this.clientPublicKeyStorage = clientPublicKeyStorage;
        this.nonceCache = nonceCache;
        this.privatePBSKeyStorage = privatePBSKeyStorage;
    }

    //sure that each object should have null check but skipped it, left only logic
    public SignedNonce authenticate(ActionAuthRequest actionAuthRequest) {
        // to make sure that in set of available to access clients
        if (!clientPublicKeyStorage.isClientKey(actionAuthRequest.clientId(), actionAuthRequest.publicKey())) {
            return null;
        }
        String expectedCertificate = TemplateGenerationUtil.authenticationRequest(actionAuthRequest.actionType(), actionAuthRequest.bucketId());
        if (!EncryptionUtil.verify(expectedCertificate, actionAuthRequest.certificate(), actionAuthRequest.publicKey())) {
            return null;
        }
        String nonce = NonceGenerationUtil.generateNonce();
        long timestamp = nonceCache.addNonce(nonce, actionAuthRequest.bucketId(), actionAuthRequest.clientId());
        String toEncode = TemplateGenerationUtil.authenticationResponse(actionAuthRequest.actionType(), actionAuthRequest.bucketId(), timestamp, nonce);
        String signature = EncryptionUtil.encodeWithPublicKey(toEncode, actionAuthRequest.publicKey());
        return new SignedNonce(timestamp, actionAuthRequest, signature);
    }

    //sure that each object should have null check but skipped it, left only logic
    public boolean isAuthenticated(SignedActionRequest signedActionRequest) {
        ActionAuthRequest actionAuthRequest = signedActionRequest.signedNonce().actionAuthRequest();
        ActionType actionType = actionAuthRequest.actionType();
        Integer bucketId = actionAuthRequest.bucketId();
        long timestamp = signedActionRequest.signedNonce().timestamp();

        //step 0, make sure that public key wasn't speculated
        if (!clientPublicKeyStorage.isClientKey(actionAuthRequest.clientId(), actionAuthRequest.publicKey())) {
            // if nonce actual and presented -> spoiled
            nonceCache.removeNonce(signedActionRequest.nonce());
            return false;
        }

        //step 1: make sure that certificate is valid and nonce used by the same client
        String expectedSignature = TemplateGenerationUtil.signedActionRequest(actionType, bucketId, timestamp);
        if (!EncryptionUtil.verify(expectedSignature, signedActionRequest.certificate(), actionAuthRequest.publicKey())) {
            // if nonce actual and presented -> spoiled
            nonceCache.removeNonce(signedActionRequest.nonce());
            return false;
        }

        //step 2: make sure that nonce is valid and the same client still want to manipulate with the same bucket to which asked access (you said that for action it's not required extra check)
        return nonceCache.validateAndRemove(signedActionRequest.nonce(), bucketId, actionAuthRequest.clientId());
    }

    public String generateResponseActionSignature(ActionType actionType, Integer bucketId) {
        String toEncode = TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        return EncryptionUtil.encodeWithPrivateKey(toEncode, privatePBSKeyStorage.myPrivateKey());
    }
}
