package org.seerbit.transtatistics.model;

import lombok.ToString;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ToString
public class Statistics {
    private Lock lock = new ReentrantLock();
    private BigDecimal sum = BigDecimal.ZERO;
    private BigDecimal max = BigDecimal.ZERO;
    private BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);
    private long count = 0;

    public Statistics() {
    }

    private Statistics(Statistics s) {
        this.sum = s.sum;
        this.max = s.max;
        this.min = s.min;
        this.count = s.count;
    }

    public void updateStatistics(BigDecimal amount) {
        try{
            lock.lock();
            sum = sum.add(amount);
            count++;
            min = min.compareTo(amount) <  0 ? min : amount;
            max = max.compareTo(amount) > 0 ? max : amount;
        }finally {
            lock.unlock();
        }
    }

    public Statistics getStatistics() {
        try{
            lock.lock();
            return new Statistics(this);
        }finally {
            lock.unlock();
        }
    }

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getMax() {
        return max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public long getCount() {
        return count;
    }

}
