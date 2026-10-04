package com.teago.teago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class FertilizerStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "Approved|Rejected", message = "Status must be 'Approved' or 'Rejected'")
    private String approvalStatus;

    

    public String getApprovalStatus()              { return approvalStatus; }
    public void   setApprovalStatus(String status) { this.approvalStatus = status; }
}