package org.hyperborian.bt.map;

import java.util.Map;
import java.util.TreeMap;

public class MaxSizeSortedMap<K, V> extends TreeMap<K, V> {

	private static final long serialVersionUID = -2538642425634929355L;
	
	private final int maxSize;

    public MaxSizeSortedMap(int maxSize) {
        this.maxSize = maxSize;
    }
    
    @Override
    public V put(K key, V value) {
    	V obj = super.put(key, value);
    	if(size()>maxSize) {
    		this.pollLastEntry();
    		return null;
    	} else {
    		return obj;
    	}
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
    	super.putAll(map);
    	while(size()>maxSize) {
    		this.pollLastEntry();
		}
    }
}