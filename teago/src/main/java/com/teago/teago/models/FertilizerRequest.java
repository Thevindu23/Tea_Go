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
@Table(name = "FertilizerRequest")
@Getter
@Setter
public class FertilizerRequest {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestID")
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID")
    private LandOwner landOwner;

    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID")
    private FactoryOwner factoryOwner;

    @Column(name = "FertilizerType", length = 50)
    private String fertilizerType;

    @Column(name = "Quantity", precision = 8, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "ApprovalStatus")
    private ApprovalStatus approvalStatus;

    @Column(name = "Cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "RequestDate")
    private LocalDate requestDate;

    public enum ApprovalStatus {
        Pending, Approved, Rejected
    }
}
