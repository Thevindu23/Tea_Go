package com.teago.teago.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSummaryDTO {

    private Integer landOwnerId;
    private String landOwnerName;
    private BigDecimal grossAmount;
    private BigDecimal deductionsAmount;
    private BigDecimal netAmount;
    private String status;
}
