package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class ChequeResponseDTO {

    private Integer    chequeId;
    private Integer    loanId;
    private BigDecimal loanAmount;
    private Integer    landOwnerId;
    private String     landOwnerName;
    private String     supervisorName;
    private LocalDate  issueDate;
    private String     deliveryStatus;      
    private String     verificationStatus;  

    

    public ChequeResponseDTO(Integer chequeId, Integer loanId, BigDecimal loanAmount,
                             Integer landOwnerId, String landOwnerName,
                             String supervisorName,
                             LocalDate issueDate, String deliveryStatus,
                             String verificationStatus) {
        this.chequeId           = chequeId;
        this.loanId             = loanId;
        this.loanAmount         = loanAmount;
        this.landOwnerId        = landOwnerId;
        this.landOwnerName      = landOwnerName;
        this.supervisorName     = supervisorName;
        this.issueDate          = issueDate;
        this.deliveryStatus     = deliveryStatus;
        this.verificationStatus = verificationStatus;
    }


    public Integer    getChequeId()           { return chequeId; }
    public Integer    getLoanId()             { return loanId; }
    public BigDecimal getLoanAmount()         { return loanAmount; }
    public Integer    getLandOwnerId()        { return landOwnerId; }
    public String     getLandOwnerName()      { return landOwnerName; }
    public String     getSupervisorName()     { return supervisorName; }
    public LocalDate  getIssueDate()          { return issueDate; }
    public String     getDeliveryStatus()     { return deliveryStatus; }
    public String     getVerificationStatus() { return verificationStatus; }
}