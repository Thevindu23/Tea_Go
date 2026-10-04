package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class LoanRequestDTO {

    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    @NotNull(message = "Factory owner ID is required")
    private Integer factoryOwnerId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1.00", message = "Loan amount must be at least Rs 1")
    private BigDecimal loanAmount;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Request type is required (LOAN or ADVANCE)")
    private String requestType = "LOAN";



    public Integer    getLandOwnerId()    { return landOwnerId; }
    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public BigDecimal getLoanAmount()     { return loanAmount; }
    public String     getReason()         { return reason; }
    public String     getRequestType()    { return requestType; }

   

    public void setLandOwnerId(Integer landOwnerId)       { this.landOwnerId = landOwnerId; }
    public void setFactoryOwnerId(Integer factoryOwnerId) { this.factoryOwnerId = factoryOwnerId; }
    public void setLoanAmount(BigDecimal loanAmount)      { this.loanAmount = loanAmount; }
    public void setReason(String reason)                  { this.reason = reason; }
    public void setRequestType(String requestType)        { this.requestType = requestType; }
}