package wavebl.integration;

import org.junit.jupiter.api.Assertions;
import wavebl.common.dto.*;
import wavebl.common.dto.action_object_impl.PutShadowAction;
import wavebl.common.entity.Shadow;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.consumer.ConsumerEntryPoint;
import wavebl.consumer.repository.InMemoryKeyStorage;
import wavebl.consumer.service.AuthenticationService;
import wavebl.pbs.PBSEntryPoint;

public class ConsumerHelper {

    public static ConsumerEntryPoint getConsumer(String consumerPublicKey, String consumerPrivateKey, String pbsPublicKey, int clientId, PBSEntryPoint pbsEntryPoint) {
        AuthenticationService authenticationService = new AuthenticationService(new InMemoryKeyStorage(consumerPublicKey, consumerPrivateKey, pbsPublicKey));
        return new ConsumerEntryPoint(clientId, authenticationService, pbsEntryPoint);
    }

    static class ConsumerSpoiled extends ConsumerEntryPoint {
        private PBSEntryPoint pbsEntryPoint;
        public ConsumerSpoiled(Integer clientId, AuthenticationService authenticationService, PBSEntryPoint pbsEntryPoint) {
            super(clientId, authenticationService, pbsEntryPoint);
            this.pbsEntryPoint = pbsEntryPoint;
        }

        @Override
        public Shadow putShadow(Integer bucketId, String color, Integer transparency) {
            ActionType actionType = ActionType.PUT_SHADOW;
            PutShadowAction putShadowAction = new PutShadowAction(color, transparency);
            String publicKey = "Aa";
            String toEncode = TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
            String certificate = "B" + toEncode;
            ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey, certificate, 1);
            SignedNonce signedNonce = pbsEntryPoint.authentication(actionAuthRequest);
            Assertions.assertNull(signedNonce);
            toEncode = TemplateGenerationUtil.signedActionRequest(actionType, bucketId, System.currentTimeMillis());
            certificate = "A" + toEncode;
            signedNonce = new SignedNonce(System.currentTimeMillis(), new ActionAuthRequest(bucketId, actionType, "Aa", certificate, 1), "");
            SignedActionRequest signedActionRequest = new SignedActionRequest(putShadowAction, signedNonce, "please give me data", certificate);
            Assertions.assertNull(pbsEntryPoint.makeAction(signedActionRequest));
            return null;
        }
    }

    public static ConsumerEntryPoint getSpoiledConsumer(PBSEntryPoint pbsEntryPoint) {
        return new ConsumerSpoiled(1, null, pbsEntryPoint);
    }
}
