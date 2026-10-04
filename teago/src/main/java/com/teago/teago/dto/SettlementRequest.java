package com.teago.teago.dto;
import jakarta.validation.constraints.*;
public record SettlementRequest(@NotNull Integer landOwnerId, @NotNull @Min(1) @Max(12) Integer month,
    @NotNull @Min(2000) @Max(3000) Integer year, @NotBlank @Pattern(regexp="BANK_TRANSFER|CHEQUE") String method,
    @NotBlank @Size(max=100) String reference) {}
