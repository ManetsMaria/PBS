package wavebl.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import wavebl.common.entity.Shadow;
import wavebl.consumer.ConsumerEntryPoint;
import wavebl.pbs.PBSEntryPoint;

public class SimpleIntegrationTest {

    @Test
    public void simpleMakeActionFlow() {
        PBSHelper pbsTestHelper = new PBSHelper();
        PBSEntryPoint pbsEntryPoint = pbsTestHelper.getPBS();
        ConsumerEntryPoint consumerEntryPoint = ConsumerHelper.getConsumer("Aa", "bA", "356", 1, pbsEntryPoint);

        Shadow shadow = consumerEntryPoint.putShadow(pbsTestHelper.bucketId1, "color", 1);
        Assertions.assertNotNull(shadow);
        Integer id = shadow.id();
        Assertions.assertNotNull(id);
        Assertions.assertEquals("color", shadow.color());

        Shadow getShadow = consumerEntryPoint.getShadow(pbsTestHelper.bucketId1, id);
        Assertions.assertNotNull(getShadow);
        Assertions.assertEquals("color", shadow.color());

        getShadow = consumerEntryPoint.getShadow(pbsTestHelper.bucketId2, id);
        Assertions.assertNull(getShadow);
    }

    @Test
    public void spoiledConsumerTest() {
        PBSHelper pbsTestHelper = new PBSHelper();
        PBSEntryPoint pbsEntryPoint = pbsTestHelper.getPBS();
        ConsumerEntryPoint consumerEntryPoint = ConsumerHelper.getSpoiledConsumer(pbsEntryPoint);
        consumerEntryPoint.putShadow(pbsTestHelper.bucketId1, "color", 1);
    }

    @Test
    public void spoiledPBSTest() {
        PBSHelper pbsTestHelper = new PBSHelper();
        PBSEntryPoint pbsEntryPoint = pbsTestHelper.getSpoiledPBS();
        ConsumerEntryPoint consumerEntryPoint = ConsumerHelper.getConsumer("Aa", "bA", "356", 1, pbsEntryPoint);
        Assertions.assertNull(consumerEntryPoint.putShadow(pbsTestHelper.bucketId1, "color", 1));
    }
}
