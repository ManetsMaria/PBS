package wavebl.common.dto;

public record SignedActionRequest(ActionObject actionObject, SignedNonce signedNonce, String nonce, String certificate) {
}
