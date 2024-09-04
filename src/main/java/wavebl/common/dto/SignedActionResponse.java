package wavebl.common.dto;

public record SignedActionResponse(ActionResponseObject actionResponseObject, String certificate) {
}
