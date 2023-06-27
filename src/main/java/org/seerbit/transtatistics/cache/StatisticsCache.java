package org.seerbit.transtatistics.cache;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StatisticsCache<Long, Statistics> extends LinkedHashMap<Long, Statistics> {

    private static final int MIN_CACHE_CAPACITY = 30;
    private static final int MAX_CACHE_CAPACITY = 30000;
    private int capacity = MIN_CACHE_CAPACITY;

    public StatisticsCache(){
        super();
    }

    public StatisticsCache(int capacity){
        super();
        if(capacity < 30){
            capacity = MIN_CACHE_CAPACITY;
        } else if(capacity > MAX_CACHE_CAPACITY){
            capacity = MAX_CACHE_CAPACITY;
        } else if(capacity % 3 != 0){
            int rem = capacity % 3;
            capacity += (3 - rem);
        }
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, Statistics> eldest) {
        return this.size() > capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}