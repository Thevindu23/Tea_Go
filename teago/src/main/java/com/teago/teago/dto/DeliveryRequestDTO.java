package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


public class DeliveryRequestDTO {

    
    @NotNull(message = "Factory owner ID is required")
    private Integer factoryOwnerId;

    
    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    
    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    
    @NotNull(message = "Tea weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    private BigDecimal teaWeight;

    
    @jakarta.validation.constraints.DecimalMin("0.01")
    private BigDecimal ratePerKg;

    
    @jakarta.validation.constraints.DecimalMin("0.00")
    private BigDecimal deductionAmount = BigDecimal.ZERO;

    

    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public Integer    getLandOwnerId()    { return landOwnerId; }
    public LocalDate  getDeliveryDate()   { return deliveryDate; }
    public BigDecimal getTeaWeight()      { return teaWeight; }
    public BigDecimal getRatePerKg()      { return ratePerKg; }
    public BigDecimal getDeductionAmount() { return deductionAmount != null ? deductionAmount : BigDecimal.ZERO; }

    
    public void setFactoryOwnerId(Integer factoryOwnerId) { this.factoryOwnerId = factoryOwnerId; }
    public void setLandOwnerId(Integer landOwnerId)       { this.landOwnerId = landOwnerId; }
    public void setDeliveryDate(LocalDate deliveryDate)   { this.deliveryDate = deliveryDate; }
    public void setTeaWeight(BigDecimal teaWeight)        { this.teaWeight = teaWeight; }
    public void setRatePerKg(BigDecimal ratePerKg)        { this.ratePerKg = ratePerKg; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount != null ? deductionAmount : BigDecimal.ZERO; }
}