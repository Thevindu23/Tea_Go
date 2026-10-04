package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class FactoryRegistrationRequestDTO {

    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    @NotNull(message = "Factory ID is required")
    private Integer factoryId;

    @DecimalMin(value = "0.01", message = "Land size must be greater than zero")
    private BigDecimal landSize;

    

    public Integer    getLandOwnerId() { return landOwnerId; }
    public Integer    getFactoryId()   { return factoryId; }
    public BigDecimal getLandSize()    { return landSize; }

    public void setLandOwnerId(Integer landOwnerId) { this.landOwnerId = landOwnerId; }
    public void setFactoryId(Integer factoryId)     { this.factoryId = factoryId; }
    public void setLandSize(BigDecimal landSize)    { this.landSize = landSize; }
}