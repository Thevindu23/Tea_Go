package com.teago.teago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


 
public class ComplaintRequestDTO {

    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    @NotNull(message = "Factory owner ID is required")
    private Integer factoryOwnerId;

    @NotBlank(message = "Complaint text is required")
    private String complaintText;



    public Integer getLandOwnerId()                    { return landOwnerId; }
    public Integer getFactoryOwnerId()                 { return factoryOwnerId; }
    public String  getComplaintText()                  { return complaintText; }

    public void setLandOwnerId(Integer landOwnerId)    { this.landOwnerId = landOwnerId; }
    public void setFactoryOwnerId(Integer factoryOwnerId) { this.factoryOwnerId = factoryOwnerId; }
    public void setComplaintText(String complaintText) { this.complaintText = complaintText; }
}