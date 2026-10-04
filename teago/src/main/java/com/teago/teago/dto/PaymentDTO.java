package com.teago.teago.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
 
    private Integer   paymentId;
    private Integer   deliveryId;
    private LocalDate deliveryDate;
    private BigDecimal teaWeight;
    private Integer   landOwnerId;
    private String    landOwnerName;
    private BigDecimal ratePerKg;
    private BigDecimal totalAmount;
    private LocalDate  paymentDate;
    private BigDecimal loanDeduction = BigDecimal.ZERO;
    private BigDecimal advanceDeduction = BigDecimal.ZERO;
    private BigDecimal totalDeduction = BigDecimal.ZERO;
    private BigDecimal netPayment;
}
 







