package wavebl.pbs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wavebl.common.dto.ActionAuthRequest;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.SignedActionRequest;
import wavebl.common.dto.SignedNonce;
import wavebl.common.dto.action_object_impl.GetShadowAction;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.pbs.repository.key.ClientPublicKeyStorage;
import wavebl.pbs.repository.key.PrivatePBSKeyStorage;
import wavebl.pbs.repository.nonce_cache.NonceCache;

public class AuthenticationServiceTest {

    AuthenticationService authenticationService;

    ClientPublicKeyStorage clientPublicKeyStorage;
    NonceCache nonceCache;

    int clientId1 = 1;
    String publicKey1 = "1";

    int client2 = 2;
    String publicKey2 = "2";

    String pbsPrivateKey = "123";
    String pbsPublicKey = "321";

    @BeforeEach
    public void init() {
        clientPublicKeyStorage = new ClientPublicKeyStorage();
        clientPublicKeyStorage.addKey(clientId1, publicKey1);
        clientPublicKeyStorage.addKey(client2, publicKey2);
        PrivatePBSKeyStorage privatePBSKeyStorage = new PrivatePBSKeyStorage(pbsPrivateKey);

        nonceCache = new NonceCache();
        authenticationService = new AuthenticationService(clientPublicKeyStorage, nonceCache, privatePBSKeyStorage);
    }

    @Test
    public void simpleAuthenticateTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        Assertions.assertNotNull(signedNonce);
        String signature = signedNonce.signature();
        Assertions.assertNotNull(signature);
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String prefixSignature = signature.substring(0, signatureTemplate.length());
        Assertions.assertEquals(signatureTemplate, prefixSignature);
        String nonce = signature.substring(prefixSignature.length());
        System.out.println(nonce); //just for interest
        Assertions.assertTrue(nonce.length() >= 5 && nonce.length() <= 25);
    }

    @Test
    public void simpleIsAuthenticatedTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate);
        Assertions.assertTrue(authenticationService.isAuthenticated(signedActionRequest));
    }

    @Test
    public void authenticateWithWrongKeyTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey2, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        Assertions.assertNull(signedNonce);
    }

    @Test
    public void authenticateWithWrongCertificateTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "2" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        Assertions.assertNull(signedNonce);
    }

    @Test
    public void isAuthenticatedWithWrongPublicKeyTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedNonce speculated = new SignedNonce(signedNonce.timestamp(), new ActionAuthRequest(bucketId, actionType, publicKey2, certificate, clientId1), signedNonce.signature());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, speculated, nonce, certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
        Assertions.assertFalse(authenticationService.isAuthenticated(new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate)));
    }

    @Test
    public void isAuthenticatedWithCorrectPublicKeyClientPairButByAnotherClientTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedNonce speculated = new SignedNonce(signedNonce.timestamp(), new ActionAuthRequest(bucketId, actionType, publicKey2, certificate, client2), signedNonce.signature());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, speculated, nonce, certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
        Assertions.assertFalse(authenticationService.isAuthenticated(new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate)));
    }

    @Test
    public void isAuthenticatedWithInvalidCertificateTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        certificate = TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, signedNonce, nonce, "2" + certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
        Assertions.assertFalse(authenticationService.isAuthenticated(new SignedActionRequest(getShadowAction, signedNonce, nonce, "1" + certificate)));
    }

    @Test
    public void isAuthenticatedWithWrongBucketTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        bucketId = 2;
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedNonce speculated = new SignedNonce(signedNonce.timestamp(), new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1), signedNonce.signature());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, speculated, nonce, certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
        Assertions.assertFalse(authenticationService.isAuthenticated(new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate)));
    }

    @Test
    public void isAuthenticatedWithInvalidNonceTest() {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, signedNonce, "a", certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
        Assertions.assertTrue(authenticationService.isAuthenticated(new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate)));
    }

    @Test
    public void isAuthenticatedWithExpiredNonceTest() throws InterruptedException {
        ActionType actionType = ActionType.GET_SHADOW;
        Integer bucketId = 1;
        String certificate = "1" + TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey1, certificate, clientId1);
        SignedNonce signedNonce = authenticationService.authenticate(actionAuthRequest);
        String signature = signedNonce.signature();
        String signatureTemplate = "1" + TemplateGenerationUtil.authenticationResponse(actionType, bucketId, signedNonce.timestamp(), "");
        String nonce = signature.substring(signatureTemplate.length());

        GetShadowAction getShadowAction = new GetShadowAction(1);
        Thread.sleep(2000);
        certificate = "1" + TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        SignedActionRequest signedActionRequest = new SignedActionRequest(getShadowAction, signedNonce, nonce, certificate);
        Assertions.assertFalse(authenticationService.isAuthenticated(signedActionRequest));
    }
}
