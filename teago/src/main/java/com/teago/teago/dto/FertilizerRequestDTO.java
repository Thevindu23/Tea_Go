package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class FertilizerRequestDTO {

    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    @NotNull(message = "Factory owner ID is required")
    private Integer factoryOwnerId;

    @NotBlank(message = "Fertilizer type is required")
    private String fertilizerType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;


    public Integer    getLandOwnerId()    { return landOwnerId; }
    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public String     getFertilizerType() { return fertilizerType; }
    public BigDecimal getQuantity()       { return quantity; }

    

    public void setLandOwnerId(Integer landOwnerId)       { this.landOwnerId = landOwnerId; }
    public void setFactoryOwnerId(Integer factoryOwnerId) { this.factoryOwnerId = factoryOwnerId; }
    public void setFertilizerType(String fertilizerType)  { this.fertilizerType = fertilizerType; }
    public void setQuantity(BigDecimal quantity)          { this.quantity = quantity; }
}