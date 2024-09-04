package wavebl.integration;

import wavebl.common.dto.SignedActionRequest;
import wavebl.common.dto.SignedActionResponse;
import wavebl.common.dto.action_response_impl.PutShadowResponse;
import wavebl.common.entity.Shadow;
import wavebl.pbs.PBSEntryPoint;
import wavebl.pbs.repository.buffer_bucket.InMemoryBucketRepository;
import wavebl.pbs.repository.key.ClientPublicKeyStorage;
import wavebl.pbs.repository.key.PrivatePBSKeyStorage;
import wavebl.pbs.repository.nonce_cache.NonceCache;
import wavebl.pbs.service.ActionService;
import wavebl.pbs.service.AuthenticationService;

public class PBSHelper {
    NonceCache nonceCache;
    InMemoryBucketRepository inMemoryBucketRepository;
    int bucketId1;
    int bucketId2;

    public PBSEntryPoint getPBS() {
        nonceCache = new NonceCache();
        inMemoryBucketRepository = new InMemoryBucketRepository();
        bucketId1 = inMemoryBucketRepository.createBucket().getId();
        bucketId2 = inMemoryBucketRepository.createBucket().getId();
        ClientPublicKeyStorage clientPublicKeyStorage = new ClientPublicKeyStorage();
        clientPublicKeyStorage.addKey(1, "Aa");
        clientPublicKeyStorage.addKey(2, "Bb");
        PrivatePBSKeyStorage privatePBSKeyStorage = new PrivatePBSKeyStorage("123");
        ActionService actionService = new ActionService(inMemoryBucketRepository);
        AuthenticationService authenticationService = new AuthenticationService(clientPublicKeyStorage, nonceCache, privatePBSKeyStorage);
        return new PBSEntryPoint(authenticationService, actionService);
    }

    public PBSEntryPoint getSpoiledPBS() {
        class SpoiledPBS extends PBSEntryPoint {

            public SpoiledPBS(AuthenticationService authenticationService, ActionService actionService) {
                super(authenticationService, actionService);
            }

            @Override
            public SignedActionResponse makeAction(SignedActionRequest signedActionRequest) {
                PutShadowResponse putShadowResponse = new PutShadowResponse(new Shadow(1, "c", 1));
                String signature = "fgh";
                return new SignedActionResponse(putShadowResponse, signature);
            }

        }
        nonceCache = new NonceCache();
        inMemoryBucketRepository = new InMemoryBucketRepository();
        bucketId1 = inMemoryBucketRepository.createBucket().getId();
        bucketId2 = inMemoryBucketRepository.createBucket().getId();
        ClientPublicKeyStorage clientPublicKeyStorage = new ClientPublicKeyStorage();
        clientPublicKeyStorage.addKey(1, "Aa");
        clientPublicKeyStorage.addKey(2, "Bb");
        PrivatePBSKeyStorage privatePBSKeyStorage = new PrivatePBSKeyStorage("123");
        ActionService actionService = new ActionService(inMemoryBucketRepository);
        AuthenticationService authenticationService = new AuthenticationService(clientPublicKeyStorage, nonceCache, privatePBSKeyStorage);
        return new SpoiledPBS(authenticationService, actionService);
    }
}
