package wavebl.consumer;

import wavebl.common.dto.*;
import wavebl.common.dto.action_object_impl.GetChunkAction;
import wavebl.common.dto.action_object_impl.GetShadowAction;
import wavebl.common.dto.action_object_impl.PutChunkAction;
import wavebl.common.dto.action_object_impl.PutShadowAction;
import wavebl.common.dto.action_response_impl.GetChunkResponse;
import wavebl.common.dto.action_response_impl.GetShadowResponse;
import wavebl.common.dto.action_response_impl.PutChunkResponse;
import wavebl.common.dto.action_response_impl.PutShadowResponse;
import wavebl.common.entity.Chunk;
import wavebl.common.entity.Shadow;
import wavebl.common.templates.TemplateGenerationUtil;
import wavebl.consumer.service.AuthenticationService;
import wavebl.pbs.PBSEntryPoint;

public class ConsumerEntryPoint {

    private final Integer clientId;
    private final AuthenticationService authenticationService;
    private final PBSEntryPoint pbsEntryPoint;

    public ConsumerEntryPoint(Integer clientId, AuthenticationService authenticationService, PBSEntryPoint pbsEntryPoint) {
        this.clientId = clientId;
        this.authenticationService = authenticationService;
        this.pbsEntryPoint = pbsEntryPoint;
    }

    public Shadow putShadow(Integer bucketId, String color, Integer transparency) {
        ActionType actionType = ActionType.PUT_SHADOW;
        PutShadowAction putShadowAction = new PutShadowAction(color, transparency);
        ActionResponseObject actionResponseObject = makeAction(actionType, putShadowAction, bucketId);
        if (! (actionResponseObject instanceof PutShadowResponse putShadowResponse)) {
            return null;
        }
        return putShadowResponse.shadow();
    }

    public Chunk putChunk(Integer bucketId, String shape, Integer weight) {
        ActionType actionType = ActionType.PUT_CHUNKS;
        PutChunkAction putChunkAction = new PutChunkAction(shape, weight);
        ActionResponseObject actionResponseObject = makeAction(actionType, putChunkAction, bucketId);
        if (! (actionResponseObject instanceof PutChunkResponse putChunkResponse)) {
            return null;
        }
        return putChunkResponse.chunk();
    }

    public Chunk getChunk(Integer bucketId, Integer chunkId) {
        ActionType actionType = ActionType.GET_CHUNKS;
        GetChunkAction getChunkAction = new GetChunkAction(chunkId);
        ActionResponseObject actionResponseObject = makeAction(actionType, getChunkAction, bucketId);
        if (! (actionResponseObject instanceof GetChunkResponse getChunkResponse)) {
            return null;
        }
        return getChunkResponse.chunk();
    }

    public Shadow getShadow(Integer bucketId, Integer shadowId) {
        ActionType actionType = ActionType.GET_SHADOW;
        GetShadowAction getShadowAction = new GetShadowAction(shadowId);
        ActionResponseObject actionResponseObject = makeAction(actionType, getShadowAction, bucketId);
        if (!(actionResponseObject instanceof GetShadowResponse getShadowResponse)) {
            return null;
        }
        return getShadowResponse.shadow();
    }

    private ActionResponseObject makeAction(ActionType actionType, ActionObject actionObject, Integer bucketId) {
        String publicKey = authenticationService.getPublicKey();
        String toEncode = TemplateGenerationUtil.authenticationRequest(actionType, bucketId);
        String certificate = authenticationService.generateCertificate(toEncode);
        ActionAuthRequest actionAuthRequest = new ActionAuthRequest(bucketId, actionType, publicKey, certificate, clientId);
        SignedNonce signedNonce = pbsEntryPoint.authentication(actionAuthRequest);
        if (signedNonce == null) {
            //here can be retried/another logic but do nothing
            return null;
        }
        String nonce = authenticationService.validateAndGetNonce(signedNonce);
        if (nonce == null) {
            //here can be another logic but do nothing
            return null;
        }
        toEncode = TemplateGenerationUtil.signedActionRequest(actionType, bucketId, signedNonce.timestamp());
        certificate = authenticationService.generateCertificate(toEncode);
        SignedActionRequest signedActionRequest = new SignedActionRequest(actionObject, signedNonce, nonce, certificate);
        SignedActionResponse signedActionResponse = pbsEntryPoint.makeAction(signedActionRequest);
        if (signedActionResponse == null) {
            //here can be another logic but do nothing
            return null;
        }
        if (!authenticationService.validateResponseActionCertificate(signedActionResponse.certificate(), actionType, bucketId)) {
            //here can be another logic but do nothing
            return null;
        }
        return signedActionResponse.actionResponseObject();
    }

}
