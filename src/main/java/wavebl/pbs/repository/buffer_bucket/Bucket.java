package wavebl.pbs.repository.buffer_bucket;


import wavebl.common.entity.Chunk;
import wavebl.common.entity.Shadow;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// I skipped shadows and chunks repositories to make this part of task as simple as possible
public class Bucket {
    private final Integer id;
    private final ConcurrentMap<Integer, Chunk> chunks;
    private final ConcurrentMap<Integer, Shadow> shadows;
    private final AtomicInteger nextChunkId;
    private final AtomicInteger nextShadowId;


    public Bucket(Integer id) {
        this.id = id;
        this.chunks = new ConcurrentHashMap<>();
        this.shadows = new ConcurrentHashMap<>();
        this.nextShadowId = new AtomicInteger(0);
        this.nextChunkId = new AtomicInteger(0);
    }

    public Chunk getChunk(Integer id) {
        return id == null ? null : chunks.get(id);
    }

    public Shadow getShadow(Integer id) {
        return id == null ? null : shadows.get(id);
    }

    public Shadow putShadow(String color, Integer transparency) {
        Integer id = nextShadowId.getAndAdd(1);
        Shadow shadow = new Shadow(id, color, transparency);
        shadows.put(shadow.id(), shadow);
        return shadow;
    }

    public Chunk putChunk(String shape, Integer weight) {
        Integer id = nextChunkId.getAndAdd(1);
        Chunk chunk = new Chunk(id, shape, weight);
        chunks.put(chunk.id(), chunk);
        return chunk;
    }

    public Integer getId() {
        return id;
    }
}
