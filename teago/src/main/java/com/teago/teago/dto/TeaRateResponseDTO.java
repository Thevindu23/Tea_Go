package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class TeaRateResponseDTO {

    private Integer    rateId;
    private Integer    factoryOwnerId;
    private String     factoryName;
    private BigDecimal ratePerKg;
    private LocalDate  effectiveFrom;

    

    public TeaRateResponseDTO(Integer rateId, Integer factoryOwnerId, String factoryName,
                               BigDecimal ratePerKg, LocalDate effectiveFrom) {
        this.rateId         = rateId;
        this.factoryOwnerId = factoryOwnerId;
        this.factoryName    = factoryName;
        this.ratePerKg      = ratePerKg;
        this.effectiveFrom  = effectiveFrom;
    }

    

    public Integer    getRateId()         { return rateId; }
    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public String     getFactoryName()    { return factoryName; }
    public BigDecimal getRatePerKg()      { return ratePerKg; }
    public LocalDate  getEffectiveFrom()  { return effectiveFrom; }
}