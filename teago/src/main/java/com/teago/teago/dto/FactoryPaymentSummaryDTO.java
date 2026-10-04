package com.teago.teago.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryPaymentSummaryDTO {

    private BigDecimal totalPayable;
    private BigDecimal totalDeductions;
    private BigDecimal netDisbursable;
    private Integer paymentsDone;
    private Integer totalLandOwners;
}
