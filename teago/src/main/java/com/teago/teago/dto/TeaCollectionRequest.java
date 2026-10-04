package com.teago.teago.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record TeaCollectionRequest(@NotNull Integer employeeId, @NotNull @PastOrPresent LocalDate collectionDate,
    @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal quantityKg,
    @NotBlank @Pattern(regexp="Green|Withered|Good|Bruised|Other") String quality, @Size(max=500) String notes) {}
