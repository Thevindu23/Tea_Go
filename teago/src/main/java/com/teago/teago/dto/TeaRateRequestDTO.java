package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


public class TeaRateRequestDTO {

    @NotNull(message = "Factory owner ID is required")
    private Integer factoryOwnerId;

    @NotNull(message = "Rate per kg is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than zero")
    private BigDecimal ratePerKg;

    
    private LocalDate effectiveFrom;

    

    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public BigDecimal getRatePerKg()      { return ratePerKg; }
    public LocalDate  getEffectiveFrom()  { return effectiveFrom; }

    

    public void setFactoryOwnerId(Integer factoryOwnerId) { this.factoryOwnerId = factoryOwnerId; }
    public void setRatePerKg(BigDecimal ratePerKg)        { this.ratePerKg = ratePerKg; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
}