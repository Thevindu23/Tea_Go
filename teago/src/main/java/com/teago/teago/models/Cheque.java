package com.teago.teago.models;

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
@Table(name = "Cheque")
@Getter
@Setter
public class Cheque {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChequeID")
    private Integer chequeId;

    @ManyToOne
    @JoinColumn(name = "LoanID", unique=true, nullable=false)
    private Loan loan;

   

    @Column(name = "IssueDate")
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "DeliveryStatus")
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "VerificationStatus")
    private VerificationStatus verificationStatus;

    public enum DeliveryStatus {
        Pending, In_Transit, Delivered, Returned
    }

    public enum VerificationStatus {
        Pending, Verified, Rejected
    }

}

