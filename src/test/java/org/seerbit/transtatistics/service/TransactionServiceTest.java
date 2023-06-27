package org.seerbit.transtatistics.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.seerbit.transtatistics.cache.StatisticsCache;
import org.seerbit.transtatistics.dto.StatisticsResponse;
import org.seerbit.transtatistics.dto.TransactionRequest;
import org.seerbit.transtatistics.exception.TransactionException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionServiceTest {

    TransactionService transactionService = new TransactionService(new StatisticsCache<>());

    @Order(1)
    @Test
    void given_validRequestButOlderThan30Secs_when_addTransaction_should_returnHttpStatusNO_CONTENT() throws TransactionException {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTimestamp(LocalDateTime.now().minusSeconds(40));
        transactionRequest.setAmount(BigDecimal.ONE);
        HttpStatus result = transactionService.addTransaction(transactionRequest, LocalDateTime.now());
        Assertions.assertEquals(HttpStatus.NO_CONTENT, result);
    }

    @Order(2)
    @Test
    void given_validRequestButFutureDate_when_addTransaction_should_ThrowException() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTimestamp(LocalDateTime.now().plusSeconds(40));
        transactionRequest.setAmount(BigDecimal.ONE);
        Assertions.assertThrows(TransactionException.class, () -> transactionService.addTransaction(transactionRequest, LocalDateTime.now()));
    }

    @Order(3)
    @Test
    void given_validRequest_when_addTransaction_should_returnHttpStatusCREATED() throws TransactionException {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTimestamp(LocalDateTime.now().minusSeconds(4));
        transactionRequest.setAmount(BigDecimal.ONE);
        HttpStatus result = transactionService.addTransaction(transactionRequest, LocalDateTime.now());
        Assertions.assertEquals(HttpStatus.CREATED, result);
    }

    @Order(4)
    @Test
    void given_validRequest_when_getStatistics_should_returnStatistics() throws TransactionException {

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTimestamp(LocalDateTime.now().minusSeconds(4));
        transactionRequest.setAmount(BigDecimal.ONE);
        HttpStatus result = transactionService.addTransaction(transactionRequest, LocalDateTime.now());
        Assertions.assertEquals(HttpStatus.CREATED, result);

        StatisticsResponse statisticsResponse = transactionService.getStatistics(LocalDateTime.now());
        Assertions.assertNotNull(statisticsResponse);
        Assertions.assertEquals(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP), statisticsResponse.getSum());
        Assertions.assertEquals(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP), statisticsResponse.getMin());
        Assertions.assertEquals(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP), statisticsResponse.getMax());
        Assertions.assertEquals(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP), statisticsResponse.getAvg());
    }

    @Order(5)
    @Test
    void given_validRequest_when_clearCache_should_doesNotThrowExceptionAndClearStats() {
        Assertions.assertDoesNotThrow(() -> transactionService.clearCache());
        StatisticsResponse result = transactionService.getStatistics(LocalDateTime.now());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getSum());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getMin());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getMax());
        Assertions.assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getAvg());
    }
}