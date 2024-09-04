package wavebl.pbs.repository.buffer_bucket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryBucketRepository{

    private final ConcurrentMap<Integer, Bucket> buckets;
    private final AtomicInteger nextId;

    public InMemoryBucketRepository() {
        this.buckets = new ConcurrentHashMap<>();
        this.nextId = new AtomicInteger(0);
    }

    public Bucket getBucket(Integer id) {
        return id == null ? null : this.buckets.get(id);
    }

    public Bucket createBucket() {
        Integer id = nextId.getAndAdd(1);
        Bucket bucket = new Bucket(id);
        this.buckets.put(bucket.getId(), bucket);
        return bucket;
    }
}
