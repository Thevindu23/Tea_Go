package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class FertilizerResponseDTO {

    private Integer    requestId;
    private Integer    landOwnerId;
    private String     landOwnerName;
    private Integer    factoryOwnerId;
    private String     factoryOwnerName;
    private String     fertilizerType;
    private BigDecimal quantity;
    private BigDecimal cost;
    private String     approvalStatus;   // "Pending" | "Approved" | "Rejected"
    private LocalDate  requestDate;

   

    public FertilizerResponseDTO(Integer requestId, Integer landOwnerId,
                                 String landOwnerName,
                                 Integer factoryOwnerId, String factoryOwnerName,
                                 String fertilizerType,
                                 BigDecimal quantity, BigDecimal cost,
                                 String approvalStatus,
                                 LocalDate requestDate) {
        this.requestId      = requestId;
        this.landOwnerId    = landOwnerId;
        this.landOwnerName  = landOwnerName;
        this.factoryOwnerId = factoryOwnerId;
        this.factoryOwnerName = factoryOwnerName;
        this.fertilizerType = fertilizerType;
        this.quantity       = quantity;
        this.cost           = cost;
        this.approvalStatus = approvalStatus;
        this.requestDate    = requestDate;
    }

    

    public Integer    getRequestId()      { return requestId; }
    public Integer    getLandOwnerId()    { return landOwnerId; }
    public String     getLandOwnerName()  { return landOwnerName; }
    public Integer    getFactoryOwnerId() { return factoryOwnerId; }
    public String     getFactoryOwnerName() { return factoryOwnerName; }
    public String     getFertilizerType() { return fertilizerType; }
    public BigDecimal getQuantity()       { return quantity; }
    public BigDecimal getCost()           { return cost; }
    public String     getApprovalStatus() { return approvalStatus; }
    public LocalDate  getRequestDate()    { return requestDate; }
}