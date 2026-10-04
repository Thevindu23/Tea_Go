package com.teago.teago.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record DailyWageRequest(@NotNull Integer employeeId, @NotNull @PastOrPresent LocalDate wageDate,
    @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal dailyRate,
     @Size(max=500) String notes) {}
