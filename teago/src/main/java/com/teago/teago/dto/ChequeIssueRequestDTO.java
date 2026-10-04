package com.teago.teago.dto;

import jakarta.validation.constraints.NotNull;


public class ChequeIssueRequestDTO {

    @NotNull(message = "Loan ID is required")
    private Integer loanId;

   

    public Integer getLoanId() { return loanId; }

    public void setLoanId(Integer loanId) { this.loanId = loanId; }
}
