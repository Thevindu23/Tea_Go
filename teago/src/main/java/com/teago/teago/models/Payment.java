package com.teago.teago.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Payment")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "DeliveryID", unique=true, nullable=false)
    private Delivery delivery;

    @Column(name = "RatePerKg", precision = 6, scale = 2)
    private BigDecimal ratePerKg;

    @Column(name = "TotalAmount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "PaymentDate")
    private LocalDate paymentDate;

    @Column(name = "LoanDeduction", precision = 10, scale = 2)
    private BigDecimal loanDeduction = BigDecimal.ZERO;

    @Column(name = "AdvanceDeduction", precision = 10, scale = 2)
    private BigDecimal advanceDeduction = BigDecimal.ZERO;

    @Column(name = "TotalDeduction", precision = 10, scale = 2)
    private BigDecimal totalDeduction = BigDecimal.ZERO;

    @Column(name = "NetPayment", precision = 10, scale = 2)
    private BigDecimal netPayment;
}
