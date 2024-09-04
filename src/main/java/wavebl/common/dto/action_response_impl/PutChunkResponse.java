package wavebl.common.dto.action_response_impl;

import wavebl.common.dto.ActionResponseObject;
import wavebl.common.entity.Chunk;

public record PutChunkResponse (Chunk chunk) implements ActionResponseObject {
}
