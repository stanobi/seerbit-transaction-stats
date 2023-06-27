package org.seerbit.transtatistics.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.seerbit.transtatistics.dto.StatisticsResponse;
import org.seerbit.transtatistics.dto.TransactionRequest;
import org.seerbit.transtatistics.exception.TransactionException;
import org.seerbit.transtatistics.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping({"/transaction", "/transactions"})
    public ResponseEntity postTransaction(@Valid @RequestBody TransactionRequest request) throws TransactionException {
        HttpStatus httpStatus = transactionService.addTransaction(request, LocalDateTime.now());
        return ResponseEntity.status(httpStatus).build();
    }

    @GetMapping(value = {"/statistics", "/transaction"}, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStatistics() {
        StatisticsResponse statistics = transactionService.getStatistics(LocalDateTime.now());
        return ResponseEntity.ok(statistics);
    }

    @DeleteMapping({"/transaction", "/transactions"})
    public ResponseEntity clearTransactions() {
        transactionService.clearCache();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
