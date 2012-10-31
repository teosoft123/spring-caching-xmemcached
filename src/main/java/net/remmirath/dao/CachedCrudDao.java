package net.remmirath.dao;

import java.util.Random;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import com.thimbleware.jmemcached.Key;

public class CachedCrudDao implements CrudDaoInterface {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CrudAlertListener alertListener;

    private Random r = new Random();

    Long data;
    Integer id;

    @Override
    public Integer create(Long data) {
        alertListener.alertCreate();
        log();
        if(null != this.id) {
            throw new IllegalStateException("Data exists, cannot override, call delete first");
        }
        this.data = data;
        id = r.nextInt();
        return id;
    }

    @Override
    @Cacheable("memcached")
    public Long read(final int id) {
        alertListener.alertRead();
        log(id);
        if(null != this.id && id == this.id) {
            return data;
        }
        return null; // not found
    }

    @Override
    @CacheEvict(key="#id", value="memcached")
    public void update(final int id, Long newData) {
        alertListener.alertUpdate();
        log(id);
        if(id == this.id) {
            this.data = newData;
        }
    }

    @Override
    @CacheEvict("memcached")
    public void delete(final int id) {
        alertListener.alertDelete();
        log(id);
        this.data = null;
        this.id = null;
    }

    private void log() {
        String methodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
        log.debug("{}()", methodName);
    }

    private void log(Object key) {
        String methodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
        log.debug("{}({})", methodName, keyToString(key));
    }

    private String keyToString(Object key) {
        if(key instanceof Key) {
            return new String(Key.class.cast(key).bytes.array());
        }
        else return null != key ? key.toString() : "null";
    }
}
