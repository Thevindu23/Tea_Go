package com.teago.teago.dto;

import java.time.LocalDate;


public class ComplaintResponseDTO {

    private Integer   complaintId;
    private Integer   landOwnerId;
    private String    landOwnerName;
    private Integer   factoryOwnerId;
    private String    factoryOwnerName;
    private String    complaintText;
    private String    responseText;     
    private String    status;           
    private LocalDate dateSubmitted;


    public ComplaintResponseDTO(Integer complaintId, Integer landOwnerId,
                                String landOwnerName,
                                Integer factoryOwnerId, String factoryOwnerName,
                                String complaintText,
                                String responseText, String status,
                                LocalDate dateSubmitted) {
        this.complaintId   = complaintId;
        this.landOwnerId   = landOwnerId;
        this.landOwnerName = landOwnerName;
        this.factoryOwnerId = factoryOwnerId;
        this.factoryOwnerName = factoryOwnerName;
        this.complaintText = complaintText;
        this.responseText  = responseText;
        this.status        = status;
        this.dateSubmitted = dateSubmitted;
    }

 

    public Integer   getComplaintId()   { return complaintId; }
    public Integer   getLandOwnerId()   { return landOwnerId; }
    public String    getLandOwnerName() { return landOwnerName; }
    public Integer   getFactoryOwnerId() { return factoryOwnerId; }
    public String    getFactoryOwnerName() { return factoryOwnerName; }
    public String    getComplaintText() { return complaintText; }
    public String    getResponseText()  { return responseText; }
    public String    getStatus()        { return status; }
    public LocalDate getDateSubmitted() { return dateSubmitted; }
}