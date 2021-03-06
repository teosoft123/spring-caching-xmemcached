package net.remmirath.spring.caching;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.remmirath.spring.caching.MemcachedCacheManager;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

public class MemcachedCacheTest {

    private static CacheManager cacheManager;
    private static Cache cache;
    private static MemCacheDaemon<LocalCacheElement> daemon;
    private static MemcachedClient client;

    @BeforeClass
    public static void setup() throws IOException {
        //TODO find available server port dynamically (see how apache mina does that)
        int port = 11212;
        // create daemon and start it
        daemon = new MemCacheDaemon<LocalCacheElement>();

        CacheStorage<Key, LocalCacheElement> storage =
                ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 10000, 10000);

        daemon.setCache(new CacheImpl(storage));
        // daemon.setBinary(true);
        daemon.setAddr(new InetSocketAddress("localhost", port));
        // daemon.setIdleTime(10000);
        // daemon.setVerbose(true);
        daemon.start();

        String servers = "localhost:" + port;
        System.setProperty("spymemcachedservers", servers);

        List<InetSocketAddress> addr = new ArrayList<InetSocketAddress>();
        addr.add(new InetSocketAddress("localhost", port));
        client = new XMemcachedClient(addr);
        // AddrUtil.getAddresses(servers));
        cacheManager = new MemcachedCacheManager(client, 180);
        assertNotNull(cacheManager);
        cache = cacheManager.getCache("unittest");
        assertNotNull(cache);
    }

    @AfterClass
    public static void teardown() throws IOException {
        client.shutdown();
        daemon.stop();
    }

    @Test
    public void testNonExisting() {
        ValueWrapper value = cache.get(UUID.randomUUID());
        assertNull(value);
    }

    @Test
    public void testPutAndGet() {
        UUID key = UUID.randomUUID();
        cache.put(key, key);
        ValueWrapper value = cache.get(key);
        assertNotNull(value);
        assertNotNull(value.get());
        assertEquals(key, value.get());
    }

    @Test(enabled = false)
    public void mustSupportStoringNullValues() {
        UUID key = UUID.randomUUID();
        cache.put(key, null);
        ValueWrapper value = cache.get(key);
        assertNotNull(value);
        assertNull(value.get());
        assertEquals(null, value.get());
    }

    @Test
    public void testMultiple() {
        for (int i = 0; i < 100; i++) {
            UUID key = UUID.randomUUID();
            cache.put(key, key);
            ValueWrapper value = cache.get(key);
            assertNotNull(value);
            assertNotNull(value.get());
            assertEquals(key, value.get());
        }
    }

    @Test
    public void testDelete() {
        UUID key = UUID.randomUUID();
        cache.put(key, key);
        ValueWrapper value = cache.get(key);
        assertNotNull(value);
        assertNotNull(value.get());
        assertEquals(key, value.get());

        cache.evict(key);

        value = cache.get(key);
        assertNull(value);
    }

}
