package net.remmirath.spring.caching;

import java.io.Serializable;
import java.util.Set;

import net.remmirath.dao.SerializablePackage;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.internal.annotations.Sets;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSerializingTranscoder extends SerializingTranscoder {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).disableHtmlEscaping().create();

    private static final Set<Package> serializablePackages = Sets.newHashSet();
    
    static {
    	serializablePackages.add(SerializablePackage.class.getPackage());
    }
    
    public static class TypedJson implements Serializable {

    	private static final long serialVersionUID = 8704302206670152638L;
		public TypedJson(String javaClass, String json) {
            this.javaClass = javaClass;
            this.json = json;
        }
        public final String javaClass;
        public final String json;
    }

    @Override
    protected byte[] serialize(Object o) {
        String json = null;
        if (o == null) {
            throw new NullPointerException("Can't serialize null");
        }
        if(isInSerializablePackage(o)) {
            json = gson.toJson(o);
            o = new TypedJson(o.getClass().getCanonicalName(), json);
        }
        return super.serialize(o);
    }

	@Override
    protected Object deserialize(byte[] in) {
        Object o = super.deserialize(in);
        if(o instanceof TypedJson) {
            TypedJson typedJson = TypedJson.class.cast(o);
            Class<?> javaClass;
            try {
                javaClass = Class.forName(typedJson.javaClass); // TODO consider classloader parameter
            } catch (ClassNotFoundException e) {
                //TODO LOG
                throw new IllegalArgumentException("Serialized class cannot be instantiated", e);
            }
            o = gson.fromJson(typedJson.json, javaClass);
        }
        return o;
    }

	private boolean isInSerializablePackage(Object o) {
		return serializablePackages.contains(o.getClass().getPackage());
	}

}
