package wavebl.pbs;

import wavebl.common.dto.*;
import wavebl.pbs.service.ActionService;
import wavebl.pbs.service.AuthenticationService;

public class PBSEntryPoint {

    private final AuthenticationService authenticationService;
    private final ActionService actionService;

    public PBSEntryPoint(AuthenticationService authenticationService, ActionService actionService) {
        this.authenticationService = authenticationService;
        this.actionService = actionService;
    }

    public SignedNonce authentication(ActionAuthRequest actionAuthRequest) {
        return authenticationService.authenticate(actionAuthRequest);
    }

    public SignedActionResponse makeAction(SignedActionRequest signedActionRequest) {
        if (!authenticationService.isAuthenticated(signedActionRequest)) {
            return null;
        }
        ActionType actionType = signedActionRequest.signedNonce().actionAuthRequest().actionType();
        ActionObject actionObject = signedActionRequest.actionObject();
        Integer bucketId = signedActionRequest.signedNonce().actionAuthRequest().bucketId();
        ActionResponseObject actionResponseObject = actionService.makeAction(actionType, actionObject, bucketId);
        String signature = authenticationService.generateResponseActionSignature(actionType, bucketId);
        return new SignedActionResponse(actionResponseObject, signature);
    }
}
