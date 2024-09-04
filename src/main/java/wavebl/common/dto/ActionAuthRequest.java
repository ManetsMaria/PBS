package wavebl.common.dto;

// still now sure why do we need actionType on this stage if one auth for any actionType
public record ActionAuthRequest(Integer bucketId, ActionType actionType, String publicKey, String certificate, Integer clientId) {

}
