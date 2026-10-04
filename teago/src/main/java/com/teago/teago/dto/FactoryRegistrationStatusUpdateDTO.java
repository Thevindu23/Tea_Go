package com.teago.teago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public class FactoryRegistrationStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "Active|Rejected", message = "Status must be 'Active' or 'Rejected'")
    private String status;

   

    public String getStatus()             { return status; }
    public void   setStatus(String status){ this.status = status; }
}