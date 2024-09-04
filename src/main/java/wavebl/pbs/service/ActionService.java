package wavebl.pbs.service;

import wavebl.common.dto.ActionObject;
import wavebl.common.dto.ActionResponseObject;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.action_object_impl.GetChunkAction;
import wavebl.common.dto.action_object_impl.GetShadowAction;
import wavebl.common.dto.action_object_impl.PutChunkAction;
import wavebl.common.dto.action_object_impl.PutShadowAction;
import wavebl.common.dto.action_response_impl.GetChunkResponse;
import wavebl.common.dto.action_response_impl.GetShadowResponse;
import wavebl.common.dto.action_response_impl.PutChunkResponse;
import wavebl.common.dto.action_response_impl.PutShadowResponse;
import wavebl.common.entity.Chunk;
import wavebl.common.entity.Shadow;
import wavebl.pbs.repository.buffer_bucket.Bucket;
import wavebl.pbs.repository.buffer_bucket.InMemoryBucketRepository;

public class ActionService {

    private final InMemoryBucketRepository bucketRepository;

    public ActionService(InMemoryBucketRepository bucketRepository) {
        this.bucketRepository = bucketRepository;
    }

    public ActionResponseObject makeAction(ActionType actionType, ActionObject actionObject, Integer bucketId) {
        Bucket bucket = bucketRepository.getBucket(bucketId);
        if (bucket == null) {
            return null;
        }
        switch (actionType) {
            case GET_CHUNKS -> {
                return getChunk(actionObject, bucket);
            }
            case GET_SHADOW -> {
                return getShadow(actionObject, bucket);
            }
            case PUT_CHUNKS -> {
                return putChunk(actionObject, bucket);
            }
            case PUT_SHADOW -> {
                return putShadow(actionObject, bucket);
            }
            default -> {
                return null;
            }
        }
    }

    private GetChunkResponse getChunk(ActionObject actionObject, Bucket bucket) {
        if (! (actionObject instanceof GetChunkAction getChunkAction)) {
            return null;
        }
        Chunk chunk = bucket.getChunk(getChunkAction.chunkId());
        return new GetChunkResponse(chunk);
    }

    private GetShadowResponse getShadow(ActionObject actionObject, Bucket bucket) {
        if (! (actionObject instanceof GetShadowAction getShadowAction)) {
            return null;
        }
        Shadow shadow = bucket.getShadow(getShadowAction.shadowId());
        return new GetShadowResponse(shadow);
    }

    private PutChunkResponse putChunk(ActionObject actionObject, Bucket bucket) {
        if (! (actionObject instanceof PutChunkAction putChunkAction)) {
            return null;
        }
        Chunk chunk = bucket.putChunk(putChunkAction.shape(), putChunkAction.weight());
        return new PutChunkResponse(chunk);
    }

    private PutShadowResponse putShadow(ActionObject actionObject, Bucket bucket) {
        if (! (actionObject instanceof PutShadowAction putShadowAction)) {
            return null;
        }
        Shadow shadow = bucket.putShadow(putShadowAction.color(), putShadowAction.transparency());
        return new PutShadowResponse(shadow);
    }
}
