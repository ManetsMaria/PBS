package wavebl.pbs.repository.buffer_bucket;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import wavebl.common.entity.Chunk;
import wavebl.common.entity.Shadow;

//do not really cover by tests, just to make sure that there is no logical errors that can influence on main part
public class BucketTest {

    Bucket bucket = new Bucket(0);

    @Test
    public void shadowTest() {
        Shadow shadow = bucket.putShadow("1", 1);
        Assertions.assertEquals("1", shadow.color());
        Assertions.assertEquals(1, shadow.transparency());
        Assertions.assertNotNull(shadow.id());

        Integer id = shadow.id();
        Shadow shadow2 = bucket.getShadow(id);
        Assertions.assertEquals(shadow, shadow2);

        Assertions.assertNull(bucket.getShadow(-1));
    }

    @Test
    public void chunkTest() {
        Chunk chunk = bucket.putChunk("1", 1);
        Assertions.assertEquals("1", chunk.shape());
        Assertions.assertEquals(1, chunk.weight());
        Assertions.assertNotNull(chunk.id());

        Integer id = chunk.id();
        Chunk chunk1 = bucket.getChunk(id);
        Assertions.assertEquals(chunk, chunk1);

        Assertions.assertNull(bucket.getChunk(-1));
    }
}
