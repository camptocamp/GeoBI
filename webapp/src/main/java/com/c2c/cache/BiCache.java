package com.c2c.cache;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.c2c.data.DataQueryFeatureSource;
import com.c2c.style.StyleGenerationParams;

public class BiCache {
	
	private final CacheManager manager = new CacheManager();
	
	private final Cache results = new Cache("results", 100, false, false, 36000, 10800);
	private final Cache styles = new Cache("styles", 1000, false, false, 36000, 10800);
	
	{
		manager.addCache(results);		
		manager.addCache(styles);
	}
	
	@SuppressWarnings("unchecked")
	private <T>T get(String id, Cache cache) {
		Element result = cache.get(id);
		if (result == null) {
			throw new CacheMissException(id,cache.getName());
		}
		return (T)result.getObjectValue();
	}
	
	public synchronized DataQueryFeatureSource getResults(String id) {
		return get(id, results);
	}
	
	public synchronized String putResults(DataQueryFeatureSource result)
		throws NoSuchAlgorithmException, UnsupportedEncodingException {

		String id = sha1(result.getMdx());
		results.put(new Element(id, result));
		return id;
	}

	public synchronized StyleGenerationParams getStyle(String id) {
		return get(id, styles);
	}
	
	public synchronized String putStyle(StyleGenerationParams style) {
		
		String id = UUID.randomUUID().toString();
		styles.put(new Element(id, style));
		return id;
	}
	
    private static String sha1(String query) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(query.getBytes());
        return convertToHex(digest);
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfByte = (data[i] >>> 4) & 0x0F;
            int twoHalves = 0;
            do {
                if ((0 <= halfByte) && (halfByte <= 9))
                    buf.append((char) ('0' + halfByte));
                else
                    buf.append((char) ('a' + (halfByte - 10)));
                halfByte = data[i] & 0x0F;
            } while (twoHalves++ < 1);
        }
        return buf.toString();
    }
	
}
