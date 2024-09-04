package wavebl.consumer.service;

import wavebl.common.dto.ActionAuthRequest;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.SignedNonce;
import wavebl.common.encryption.EncryptionUtil;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.consumer.repository.InMemoryKeyStorage;

public class AuthenticationService {

    private final InMemoryKeyStorage inMemoryKeyStorage;

    public AuthenticationService(InMemoryKeyStorage inMemoryKeyStorage) {
        this.inMemoryKeyStorage = inMemoryKeyStorage;
    }

    public String generateCertificate(String toEncode) {
        return EncryptionUtil.encodeWithPrivateKey(toEncode, inMemoryKeyStorage.myPrivateKey());
    }

    public String validateAndGetNonce(SignedNonce signedNonce) {
        long currentTime = System.currentTimeMillis();
        //should be configured time gap also but hardcoded
        if (currentTime >= signedNonce.timestamp() + 2*1000L) {
            return null;
        }
        String signature = signedNonce.signature();
        if (signature == null) {
            return null;
        }
        String decoded = decodeSignature(signature);
        ActionAuthRequest actionAuthRequest = signedNonce.actionAuthRequest();
        String prefix = TemplateGenerationUtil.authenticationResponse(actionAuthRequest.actionType(), actionAuthRequest.bucketId(), signedNonce.timestamp(), "");
        if (decoded.length() < prefix.length()) {
            return null;
        }
        String decodedPrefix = decoded.substring(0, prefix.length());
        // get response not from pbs
        if (!decodedPrefix.equals(prefix)) {
            return null;
        }
        return decoded.substring(prefix.length());
    }

    public boolean validateResponseActionCertificate(String signature, ActionType actionType, Integer bucketId) {
        if (signature == null) {
            return false;
        }
        String signatureTemplate = TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        return EncryptionUtil.verify(signatureTemplate, signature, inMemoryKeyStorage.publicBPSKey());
    }

    public String getPublicKey() {
        return inMemoryKeyStorage.myPublicKey();
    }

    private String decodeSignature(String toDecode) {
        return EncryptionUtil.decode(toDecode, inMemoryKeyStorage.myPrivateKey());
    }
}
