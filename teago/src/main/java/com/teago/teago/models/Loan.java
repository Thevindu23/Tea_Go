package com.teago.teago.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Loan")
@Getter
@Setter
public class Loan {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanID")
    private Integer loanId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID")
    private LandOwner landOwner;

    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID")
    private FactoryOwner factoryOwner;

    @Column(name = "LoanAmount", precision = 10, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "Reason", length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "ApprovalStatus")
    private ApprovalStatus approvalStatus;

    @Column(name = "RequestDate")
    private LocalDate requestDate;

    @Column(name = "ApprovalDate")
    private LocalDate approvalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "RequestType")
    private RequestType requestType = RequestType.LOAN;

    @Column(name = "AdvanceDeductedMonth")
    private Integer advanceDeductedMonth;

    @Column(name = "AdvanceDeductedYear")
    private Integer advanceDeductedYear;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }

    public enum RequestType {
        LOAN, ADVANCE
    }
}

