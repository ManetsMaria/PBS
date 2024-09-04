package wavebl.pbs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wavebl.common.dto.ActionResponseObject;
import wavebl.common.dto.ActionType;
import wavebl.common.dto.action_object_impl.GetShadowAction;
import wavebl.common.dto.action_object_impl.PutShadowAction;
import wavebl.common.dto.action_response_impl.GetShadowResponse;
import wavebl.common.dto.action_response_impl.PutShadowResponse;
import wavebl.pbs.repository.buffer_bucket.InMemoryBucketRepository;

//do not really cover by tests, just to make sure that there is no logical errors that can influence on main part
public class ActionServiceTest {
    ActionService actionService;
    Integer bucketId;

    @BeforeEach
    public void init() {
        InMemoryBucketRepository inMemoryBucketRepository = new InMemoryBucketRepository();
        bucketId = inMemoryBucketRepository.createBucket().getId();
        actionService = new ActionService(inMemoryBucketRepository);
    }

    @Test
    public void shadowTest() {
        PutShadowAction putShadowAction = new PutShadowAction("1", 1);
        Assertions.assertNull(actionService.makeAction(ActionType.PUT_CHUNKS, putShadowAction, bucketId));
        Assertions.assertNull(actionService.makeAction(ActionType.PUT_SHADOW, putShadowAction, -1));
        ActionResponseObject actionResponseObject = actionService.makeAction(ActionType.PUT_SHADOW, putShadowAction, bucketId);
        PutShadowResponse putShadowResponse = Assertions.assertInstanceOf(PutShadowResponse.class, actionResponseObject);
        Assertions.assertEquals("1", putShadowResponse.shadow().color());
        Assertions.assertEquals(1, putShadowResponse.shadow().transparency());
        Integer id = putShadowResponse.shadow().id();
        Assertions.assertNotNull(id);

        GetShadowAction getShadowAction = new GetShadowAction(id);
        actionResponseObject = actionService.makeAction(ActionType.GET_SHADOW, getShadowAction, bucketId);
        GetShadowResponse getShadowResponse = Assertions.assertInstanceOf(GetShadowResponse.class, actionResponseObject);
        Assertions.assertEquals("1", getShadowResponse.shadow().color());
    }
}
