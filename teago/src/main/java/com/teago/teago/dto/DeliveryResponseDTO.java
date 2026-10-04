package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class DeliveryResponseDTO {

    private Integer    deliveryId;

    
    private Integer    factoryOwnerId;
    private String     factoryName;

    
    private Integer    landOwnerId;
    private String     landOwnerName;

    
    private LocalDate  deliveryDate;
    private BigDecimal teaWeight;
    private BigDecimal ratePerKg;
    private BigDecimal totalAmount;   // teaWeight × ratePerKg, computed in service
    private BigDecimal deductionAmount;   // Deduction applied to this delivery

    
    private Integer    month;
    private Integer    year;

    

    public DeliveryResponseDTO(Integer deliveryId,
                               Integer factoryOwnerId, String factoryName,
                               Integer landOwnerId,   String landOwnerName,
                               LocalDate deliveryDate,
                               BigDecimal teaWeight, BigDecimal ratePerKg,
                               BigDecimal totalAmount,
                               Integer month, Integer year,
                               BigDecimal deductionAmount) {
        this.deliveryId     = deliveryId;
        this.factoryOwnerId = factoryOwnerId;
        this.factoryName    = factoryName;
        this.landOwnerId    = landOwnerId;
        this.landOwnerName  = landOwnerName;
        this.deliveryDate   = deliveryDate;
        this.teaWeight      = teaWeight;
        this.ratePerKg      = ratePerKg;
        this.totalAmount    = totalAmount;
        this.month          = month;
        this.year           = year;
        this.deductionAmount = deductionAmount != null ? deductionAmount : BigDecimal.ZERO;
    }

    

    public Integer    getDeliveryId()     { return deliveryId; }
    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public String     getFactoryName()    { return factoryName; }
    public Integer    getLandOwnerId()    { return landOwnerId; }
    public String     getLandOwnerName()  { return landOwnerName; }
    public LocalDate  getDeliveryDate()   { return deliveryDate; }
    public BigDecimal getTeaWeight()      { return teaWeight; }
    public BigDecimal getRatePerKg()      { return ratePerKg; }
    public BigDecimal getTotalAmount()    { return totalAmount; }
    public Integer    getMonth()          { return month; }
    public Integer    getYear()           { return year; }
    public BigDecimal getDeductionAmount() { return deductionAmount; }
}