package net.remmirath.spring.caching;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
import com.thimbleware.jmemcached.storage.hash.SizedItem;

public class ConcurrentLinkedHashMapWithLogging<K, V extends SizedItem> extends AbstractMap<K, V> implements Serializable, CacheStorage<K, V> {

    private static final long serialVersionUID = 994936833823296904L;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    ConcurrentLinkedHashMap<K, V> delegate = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 10000, 10000);

    @Override
	public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
	public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
	public long getMemoryCapacity() {
        return delegate.getMemoryCapacity();
    }

    @Override
	public long getMemoryUsed() {
        return delegate.getMemoryUsed();
    }

    public void setCapacity(int capacity) {
        delegate.setCapacity(capacity);
    }

    public void setMemoryCapacity(int capacity) {
        delegate.setMemoryCapacity(capacity);
    }

    @Override
	public int capacity() {
        return delegate.capacity();
    }

    @Override
	public void close() {
        delegate.close();
    }

    @Override
	public int size() {
        return delegate.size();
    }

    @Override
	public void clear() {
        delegate.clear();
    }

    @Override
	public boolean containsKey(Object key) {
        log(key);
        return delegate.containsKey(key);
    }

    @Override
	public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
	public V get(Object key) {
        log(key);
        return delegate.get(key);
    }

    @Override
	public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
	public V put(K key, V value) {
        log(key, value);
        return delegate.put(key, value);
    }

    @Override
	public V putIfAbsent(K key, V value) {
        log(key, value);
        return delegate.putIfAbsent(key, value);
    }

    @Override
	public V remove(Object key) {
        log(key);
        return delegate.remove(key);
    }

    @Override
	public boolean remove(Object key, Object value) {
        log(key, value);
        return delegate.remove(key, value);
    }

    @Override
	public int hashCode() {
        return delegate.hashCode();
    }

    @Override
	public V replace(K key, V value) {
        log(key, value);
        return delegate.replace(key, value);
    }

    @Override
	public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
	public String toString() {
        return delegate.toString();
    }

    @Override
	public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
	public Collection<V> values() {
        return delegate.values();
    }

    @Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    private void log(Object key) {
        String methodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
        log.debug("{}({})", methodName, keyToString(key));
    }

    private void log(Object key, Object value) {
        String methodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
        log.debug("{}({}, {})", new Object[]{methodName, keyToString(key), value.toString()});
    }

    private String keyToString(Object key) {
        if(key instanceof Key) {
            return new String(Key.class.cast(key).bytes.array());
        }
        else return key.toString();
    }

}
