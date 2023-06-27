package org.seerbit.transtatistics.exception;

import org.springframework.http.HttpStatus;

public class TransactionException extends Exception {

    private HttpStatus httpStatus;

    public TransactionException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
