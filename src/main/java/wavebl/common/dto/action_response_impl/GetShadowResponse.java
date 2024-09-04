package wavebl.common.dto.action_response_impl;

import wavebl.common.dto.ActionResponseObject;
import wavebl.common.entity.Shadow;

public record GetShadowResponse(Shadow shadow) implements ActionResponseObject {

}
