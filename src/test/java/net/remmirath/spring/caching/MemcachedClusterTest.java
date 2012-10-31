package net.remmirath.spring.caching;

import static org.testng.AssertJUnit.assertEquals;
import net.remmirath.spring.caching.MemcachedCluster;

import org.testng.annotations.Test;
import java.net.InetSocketAddress;
import java.util.List;

public class MemcachedClusterTest {

    @Test
    public void mustParseSingleNode() {
        MemcachedCluster instance = new MemcachedCluster("localhost:1234:55;");
        assertEquals(instance.getEndpoints().size(), instance.getWeights().size());
        assertEquals(new InetSocketAddress("localhost", 1234), instance.getEndpoints().get(0));
        assertEquals(55, instance.getWeights().get(0).intValue());
    }

    @Test
    public void mustParseMultipleNodes() {
        MemcachedCluster instance = new MemcachedCluster("localhost:1234:55;localhost:3333:21");
        assertEquals(instance.getEndpoints().size(), instance.getWeights().size());
        int i = 0;
        int[] expectedWeights = new int[] {55, 21};
        int[] expectedPorts = new int[] {1234, 3333};
        List<Integer> weights = instance.getWeights();
        for(InetSocketAddress endpoint : instance.getEndpoints()) {
            assertEquals(new InetSocketAddress("localhost", expectedPorts[i]), endpoint);
            assertEquals(expectedWeights[i], weights.get(i).intValue());
            i++;
        }
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void mustHandleIllegalInput() {
        @SuppressWarnings("unused")
        MemcachedCluster instance = new MemcachedCluster("localhost:1234;");
        instance = new MemcachedCluster("localhost:1234:5;otherhost:2");
    }

}
