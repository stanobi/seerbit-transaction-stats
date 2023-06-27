package org.seerbit.transtatistics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 5)
    private BigDecimal amount;

    @NotNull(message = "timestamp is required")
    @DateTimeFormat(pattern = "YYYY-MM-DDThh:mm:ss.sssZ")
    private LocalDateTime timestamp;

}
