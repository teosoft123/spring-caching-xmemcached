package net.remmirath.spring.caching;

import java.util.Collection;

import net.rubyeye.xmemcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

public class MemcachedCacheManager extends AbstractCacheManager {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Collection<Cache> caches;
    private int expiry = -1;

    @Autowired
    private MemcachedClient memcachedClient = null;

    public MemcachedCacheManager() {
    }

    public MemcachedCacheManager(MemcachedClient client, int expiry) {
        setClient(client);
        setExpiry(expiry);
    }

    private void checkState() {
        if (memcachedClient == null) {
            throw new IllegalStateException(
                    "MemcachedClient not configured yet");
        } else if (memcachedClient.isShutdown()) {
            throw new IllegalStateException("MemcachedClient is not alive");
        }
    }

    @Override
	public Cache getCache(String name) {
        checkState();

        Cache cache = super.getCache(name);
        if (cache == null) {
            //TODO this has to be Spring-wired, eh?
            cache = new MemcachedCache(name, memcachedClient, expiry);
            addCache(cache);
        }
        return cache;
    }

    private void updateCaches() {
        if (caches != null) {
            for (Cache cache : caches) {
                if (cache instanceof MemcachedCache) {
                    MemcachedCache memcachedCache = (MemcachedCache) cache;
                    memcachedCache.setClient(memcachedClient);
                    memcachedCache.setExpiry(expiry);
                }
            }
        }
    }

    public Collection<Cache> getCaches() {
        return caches;
    }

    public MemcachedClient getClient() {
        return memcachedClient;
    }

    public int getExpiry() {
        return expiry;
    }

    @Override
    protected Collection<Cache> loadCaches() {
        return this.caches;
    }

    /**
    * Specify the collection of Cache instances to use for this CacheManager.
    */
    public void setCaches(Collection<Cache> caches) {
        this.caches = caches;
    }

    public void setClient(MemcachedClient client) {
        this.memcachedClient = client;

        updateCaches();
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;

        updateCaches();
    }

    public void shutdown() {
        // TODO

    }

}
