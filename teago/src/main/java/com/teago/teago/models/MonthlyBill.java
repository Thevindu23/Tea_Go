package com.teago.teago.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MonthlyBill", uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames={"LandOwnerID", "ScopeID", "Month", "Year"}))
@Getter
@Setter
public class MonthlyBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BillID")
    private Integer billId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID")
    private LandOwner landOwner;

    @Column(name = "ScopeID", nullable=false, columnDefinition="INTEGER DEFAULT 0")
    private Integer scopeId = 0;
    @Column(precision=12, scale=2)
    private BigDecimal loanDeductions = BigDecimal.ZERO;
    @Column(precision=12, scale=2)
    private BigDecimal fertilizerDeductions = BigDecimal.ZERO;

    @Column(name = "TotalEarnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings;

    @Column(name = "DeliveryDeductions", precision = 10, scale = 2, nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal deliveryDeductions = BigDecimal.ZERO;

    @Column(name = "TotalDeductions", precision = 10, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "NetPayable", precision = 10, scale = 2)
    private BigDecimal netPayable;

    @Column(name = "Month")
    private Integer month;

    @Column(name = "Year")
    private Integer year;

    @Column(name = "GeneratedDate")
    private LocalDate generatedDate;
}
