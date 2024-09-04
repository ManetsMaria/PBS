package wavebl.common.dto;

public record SignedNonce(long timestamp, ActionAuthRequest actionAuthRequest, String signature) {
}
