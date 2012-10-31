package net.remmirath.spring.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

public class SpringCachingServiceBean {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    public static class KeyBase {
        public final int k1;
        public final int k2;
        public KeyBase(int k1, int k2) {
            this.k1 = k1;
            this.k2 = k2;
        }

    }

    public static class K9 {
        public final Integer k1;
        public K9(Integer k1) {
            this.k1 = k1;
        }
    }

    @Autowired
    private AlertListener alertListener;

    @Cacheable(key="'key-x-1-'.concat(#key.k2 * #key.k1 + 3)", value={"memcached"})
    public int cachedMethod(KeyBase key) {
        alertListener.alert();
        return 42;
    }

    @CacheEvict(key="'key-x-1-'.concat(#key.k2 * #key.k1 + 3)", value={"memcached"})
    public void cacheEvictingMethod(KeyBase key) {
        // do nothing, only side effects matter
    }

    @Cacheable(key="#keyFoo", value={"memcached"})
    public int cachedMethodWrongSpEL(KeyBase key) {
        return 666;
    }

    @Cacheable(key="#keyFoo.foo", value={"memcached"})
    public int cachedMethodWrongSpEL2(KeyBase key) {
        return 666;
    }

    @Cacheable(key="#key?.k1", value={"memcached"}, condition="#key?.k1 != null")
    public Integer cachedMethodThatReturnsNull(K9 key) {
        return null;
    }
    
}
