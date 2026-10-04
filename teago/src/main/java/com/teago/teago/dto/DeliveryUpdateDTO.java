package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryUpdateDTO {

    @NotNull(message = "Tea weight is required")
    @DecimalMin(value = "0.01", message = "Tea weight must be greater than 0")
    private BigDecimal teaWeight;
}
