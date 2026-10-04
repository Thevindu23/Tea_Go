package com.teago.teago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


 
public class ComplaintResponseUpdateDTO {

    @NotBlank(message = "Response text is required")
    private String responseText;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "Resolved|Rejected", message = "Status must be 'Resolved' or 'Rejected'")
    private String status;

  

    public String getResponseText()              { return responseText; }
    public String getStatus()                    { return status; }

    public void setResponseText(String text)     { this.responseText = text; }
    public void setStatus(String status)         { this.status = status; }
}