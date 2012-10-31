package net.remmirath.spring.caching;

import net.rubyeye.xmemcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

public class MemcachedCache implements Cache {
    private final String name;
    private MemcachedClient memcachedClient;
    private int expiry = 3600;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public MemcachedCache(String name, MemcachedClient client, int expiry) {
        this.name = name;
        this.memcachedClient = client;
        this.expiry = expiry;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public Object getNativeCache() {
        return memcachedClient;
    }

    private static String keyToString(Object key) {
        if (key == null) {
            return null;
        } else if (key instanceof String) {
            return (String) key;
        } else {
            return key.toString();
        }
    }

    @Override
	public ValueWrapper get(Object key) {
        Object value = null;
        try {
            value = memcachedClient.get(keyToString(key));
        } catch (Exception e) {
            log.warn("Unable to get cached object", throwableOrMessage(e));
        }
        return null != value ? new SimpleValueWrapper(value) : null;
    }

    @Override
	public void put(Object key, Object value) {
        try {
            memcachedClient.set(keyToString(key), expiry, value);
        } catch (Exception e) {
            log.warn("Unable to put object to cache", throwableOrMessage(e));
        }
    }

    @Override
	public void evict(Object key) {
        try {
            memcachedClient.delete(keyToString(key));
        } catch (Exception e) {
            log.warn("Unable to delete object from cache", throwableOrMessage(e));
        }
    }

    @Override
	public void clear() {
        // Not implemented
    }

    public void setClient(MemcachedClient client) {
        this.memcachedClient = client;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }

    Object throwableOrMessage(Throwable t) {
        return t.getMessage();
    }

}
