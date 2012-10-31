package net.remmirath.spring.caching;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import net.remmirath.spring.caching.JsonSerializingTranscoder;
import net.remmirath.spring.caching.MemcachedCache;
import net.remmirath.spring.caching.MemcachedCacheManager;
import net.remmirath.spring.caching.MemcachedCluster;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.DefaultKeyGenerator;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;

@Configuration
@EnableCaching
public class SpringCachingFrameworkTestContext implements CachingConfigurer {

    private static final boolean USE_JMEMCACHED = false;

    @Bean
    public int memcachedServerPort() {
        //TODO find available server port dynamically (use Apache Mina for that)
        if(useJmemcached()) {
            return 11212;
        }
        else {
            return 11211;
        }
    }

    @Bean
    public boolean useJmemcached() {
        return USE_JMEMCACHED;
    }

    @Bean
    public MemcachedCluster memcachedClusterParser(int memcachedServerPort) {
        return new MemcachedCluster(String.format("localhost:%d:1", memcachedServerPort));
    }

    @DependsOn("memcachedDaemon")
    @Bean
    XMemcachedClientBuilder memcachedClientBuilder(MemcachedCluster memcachedClusterParser) {
        XMemcachedClientBuilder xMemcachedClientBuilder = new XMemcachedClientBuilder(memcachedClusterParser.getEndpoints(), (int[]) memcachedClusterParser.getWeightsArray());
        xMemcachedClientBuilder.setConnectionPoolSize(2);
        xMemcachedClientBuilder.setSessionLocator(new KetamaMemcachedSessionLocator());
        xMemcachedClientBuilder.setTranscoder(new JsonSerializingTranscoder());
        return xMemcachedClientBuilder;
    }

    @Bean(destroyMethod="shutdown")
    public MemcachedClient memcachedClient(XMemcachedClientBuilder memcachedClientBuilder) throws IOException {
        return memcachedClientBuilder.build();
    }

    @Bean
    public MemcachedCacheManager memcachedCacheManager(MemcachedClient memcachedClient) {
        MemcachedCacheManager bean = new MemcachedCacheManager();
        bean.setClient(memcachedClient);
        bean.setExpiry(600);
        Cache[] caches = new Cache[]{ new MemcachedCache("memcached", memcachedClient, 600)};
        bean.setCaches(Arrays.asList(caches));
        return bean;
    }

    @Bean
    @DependsOn("memcachedDaemon")
    @Override
    public CacheManager cacheManager() {
        try {
            return memcachedCacheManager(memcachedClient(memcachedClientBuilder(memcachedClusterParser(memcachedServerPort()))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new DefaultKeyGenerator();
//        return new CommonsCachingKeyGenerator();
    }

    @Bean
    public SpringCachingServiceBean service() {
        return new SpringCachingServiceBean();
    }

    @Bean
    public AlertCollator alertListener() {
        return new AlertCollator();
    }

    @Bean
    public String userNameBean(@Value("#{systemEnvironment['USER']}") String userName) {
        return userName;
    }

    class MemCacheDaemonWrapper extends MemCacheDaemon<LocalCacheElement> {
        private final boolean enabled;
        @Override
        public void stop() {
            if(enabled) {
                super.stop();
            }
        }
        @Override
        public void start() {
            if(enabled) {
                super.start();
            }
        }
        @Override
        public boolean isRunning() {
            if(enabled) {
                return super.isRunning();
            }
            return false;
        }
        public MemCacheDaemonWrapper(boolean enabled) {
            super();
            this.enabled = enabled;
        }

    }

    class MemcachedClientController {
        final MemcachedClient memcachedClient;
        public MemcachedClientController(MemcachedClient memcachedClient) {
            this.memcachedClient = memcachedClient;
        }
        public void removeAllServers() throws InterruptedException {
            Collection<InetSocketAddress> servers = memcachedClient.getAvailableServers();
            if(!servers.isEmpty()) {
                String removeServers = "";
                for(InetSocketAddress address : servers) {
                    String server = address.getHostName() + ":" + address.getPort();
                    removeServers += server + " ";
                }
                memcachedClient.removeServer(removeServers);
                Thread.sleep(1000);
            }
        }
    }

    @Bean
    public MemcachedClientController memcachedClientController(MemcachedClient memcachedClient) {
        return new MemcachedClientController(memcachedClient);
    }

    @Bean(destroyMethod="stop")
    public MemCacheDaemon<LocalCacheElement> memcachedDaemon(int memcachedServerPort, boolean useJmemcached) {
        // create daemon and start it
        MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemonWrapper(USE_JMEMCACHED);
        CacheStorage<Key, LocalCacheElement> storage = new ConcurrentLinkedHashMapWithLogging<Key, LocalCacheElement>();
        daemon.setCache(new CacheImpl(storage));
        // daemon.setBinary(true);
        daemon.setAddr(new InetSocketAddress("localhost", memcachedServerPort));
        // daemon.setIdleTime(10000);
        daemon.setVerbose(true);
        daemon.start();
        return daemon;
    }

}
