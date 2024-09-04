package wavebl.common.dto.action_object_impl;

import wavebl.common.dto.ActionObject;

public record PutChunkAction(String shape, Integer weight) implements ActionObject {

}
