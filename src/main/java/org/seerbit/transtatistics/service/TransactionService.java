package org.seerbit.transtatistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.seerbit.transtatistics.cache.StatisticsCache;
import org.seerbit.transtatistics.dto.StatisticsResponse;
import org.seerbit.transtatistics.dto.TransactionRequest;
import org.seerbit.transtatistics.exception.TransactionException;
import org.seerbit.transtatistics.model.Statistics;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final StatisticsCache<Long, Statistics> cache;

    public HttpStatus addTransaction(TransactionRequest request, LocalDateTime currentDateTime) throws TransactionException {

        LocalDateTime transactionTimestamp = request.getTimestamp();
        ensureNotFutureDateTime(transactionTimestamp, currentDateTime);

        if (isOlderThan30Seconds(transactionTimestamp, currentDateTime)) {
            return HttpStatus.NO_CONTENT;
        } else {
            Long key = getKeyFromTimestamp(transactionTimestamp);
            Statistics statistics = cache.get(key);
            if(statistics == null) {
                synchronized (cache) {
                    statistics = cache.get(key);
                    if (statistics == null) {
                        statistics = new Statistics();
                        cache.put(key, statistics);
                    }
                }
            }
            statistics.updateStatistics(request.getAmount());
        }
        return HttpStatus.CREATED;
    }

    private void ensureNotFutureDateTime(LocalDateTime transactionDateTime, LocalDateTime currentDateTime) throws TransactionException {
        if(transactionDateTime.isAfter(currentDateTime)) {
            log.error("Transaction date-time is in the future, this is not allowed");
            throw new TransactionException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Transaction datetime provided, it is in Future");
        }
    }

    private boolean isOlderThan30Seconds(LocalDateTime transactionDateTime, LocalDateTime currentDateTime) {
        LocalDateTime newDateTime = currentDateTime.minusSeconds(30);
        if(transactionDateTime.isBefore(newDateTime)) {
            log.error("Transaction date-time provided is older than 30 Seconds");
            return true;
        }
        return false;
    }

    public StatisticsResponse getStatistics(LocalDateTime timestamp) {
        Map<Long, Statistics> copy = cache.entrySet().parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStatistics()));
        return getStatisticsFromCacheCopy(copy, timestamp);
    }

    private StatisticsResponse getStatisticsFromCacheCopy(Map<Long, Statistics> copy,
                                                          LocalDateTime transactionDateTime) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal avg = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);
        long count = 0;

        Long generatedKey = getKeyFromTimestamp(transactionDateTime);
        for (Map.Entry<Long, Statistics> statisticsEntry : copy.entrySet()) {
            Long eKey = statisticsEntry.getKey();
            long timeFrame = generatedKey - eKey;
            if(timeFrame >= 0 && timeFrame < cache.getCapacity()) {
                Statistics eValue = statisticsEntry.getValue();
                if(eValue.getCount() > 0) {
                    sum = sum.add(eValue.getSum());
                    min = min.compareTo(eValue.getMin()) < 0 ? min : eValue.getMin();
                    max = max.compareTo(eValue.getMax()) > 0 ? max : eValue.getMax();
                    count += eValue.getCount();
                }
            }
        }
        if(count == 0) {
            min = BigDecimal.ZERO;
            avg = BigDecimal.ZERO;
        } else {
            avg = sum.divide(BigDecimal.valueOf(count),2 , RoundingMode.HALF_UP);
        }

        return StatisticsResponse.builder().sum(sum.setScale(2, RoundingMode.HALF_UP))
                .avg(avg.setScale(2, RoundingMode.HALF_UP))
                .max(max.setScale(2, RoundingMode.HALF_UP))
                .min(min.setScale(2, RoundingMode.HALF_UP)).count(count).build();
    }

    private Long getKeyFromTimestamp(LocalDateTime transactionDateTime) {
        return (Timestamp.valueOf(transactionDateTime).getTime() * cache.getCapacity()) / 30000;
    }

    public void clearCache() {
        cache.clear();
    }

}
