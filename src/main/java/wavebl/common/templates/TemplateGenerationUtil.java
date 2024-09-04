package wavebl.common.templates;

import wavebl.common.dto.ActionType;

public class TemplateGenerationUtil {

    public static String authenticationRequest(ActionType actionType, int bucketId) {
        return "actionType:" + actionType + "bucketId:" + bucketId;
    }

    public static String authenticationResponse(ActionType actionType, int bucketId, long timestamp, String nonce) {
        return "actionType:" + actionType + "bucketId:" + bucketId + "timestamp:" + timestamp + "nonce:" + nonce;
    }

    public static String signedActionRequest(ActionType actionType, int bucketId, long timestamp) {
        return "actionType:" + actionType + "bucketId:" + bucketId + "timestamp:" + timestamp;
    }
}
