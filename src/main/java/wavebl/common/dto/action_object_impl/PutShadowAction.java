package wavebl.common.dto.action_object_impl;

import wavebl.common.dto.ActionObject;

public record PutShadowAction (String color, Integer transparency) implements ActionObject {
}
