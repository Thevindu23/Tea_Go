package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class LoanResponseDTO {

    private Integer    loanId;
    private Integer    landOwnerId;
    private String     landOwnerName;
    private Integer    factoryOwnerId;
    private String     factoryOwnerName;
    private BigDecimal loanAmount;
    private String     reason;
    private String     approvalStatus;   
    private LocalDate  requestDate;
    private String     requestType;      


    public LoanResponseDTO(Integer loanId, Integer landOwnerId, String landOwnerName,
                           Integer factoryOwnerId, String factoryOwnerName,
                           BigDecimal loanAmount, String reason,
                           String approvalStatus, LocalDate requestDate) {
        this(loanId, landOwnerId, landOwnerName, factoryOwnerId, factoryOwnerName,
             loanAmount, reason, approvalStatus, requestDate, "LOAN");
    }

    public LoanResponseDTO(Integer loanId, Integer landOwnerId, String landOwnerName,
                           Integer factoryOwnerId, String factoryOwnerName,
                           BigDecimal loanAmount, String reason,
                           String approvalStatus, LocalDate requestDate, String requestType) {
        this.loanId         = loanId;
        this.landOwnerId    = landOwnerId;
        this.landOwnerName  = landOwnerName;
        this.factoryOwnerId = factoryOwnerId;
        this.factoryOwnerName = factoryOwnerName;
        this.loanAmount     = loanAmount;
        this.reason         = reason;
        this.approvalStatus = approvalStatus;
        this.requestDate    = requestDate;
        this.requestType    = requestType;
    }

    

    public Integer    getLoanId()          { return loanId; }
    public Integer    getLandOwnerId()     { return landOwnerId; }
    public String     getLandOwnerName()   { return landOwnerName; }
    public Integer    getFactoryOwnerId()  { return factoryOwnerId; }
    public String     getFactoryOwnerName() { return factoryOwnerName; }
    public BigDecimal getLoanAmount()      { return loanAmount; }
    public String     getReason()          { return reason; }
    public String     getApprovalStatus()  { return approvalStatus; }
    public LocalDate  getRequestDate()     { return requestDate; }
    public String     getRequestType()     { return requestType; }
}