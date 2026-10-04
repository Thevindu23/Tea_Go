package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
public class PaymentRequest {
 
    @NotNull(message = "Delivery ID is required")
    private Integer deliveryId;
 
    @NotNull(message = "Rate per kg is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    private BigDecimal ratePerKg;
 
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
}
