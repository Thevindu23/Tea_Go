package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "Delivery")
@Getter
@Setter
public class Delivery {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DeliveryID")
    private Integer deliveryId;

    
    @ManyToOne
    @JoinColumn(name = "LandOwnerID", nullable = false)
    private LandOwner landOwner;

    
    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID", nullable = false)
    private FactoryOwner factoryOwner;

    @Column(name = "DeliveryDate", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "TeaWeight", precision = 8, scale = 2, nullable = false)
    private BigDecimal teaWeight;

    @Column(name = "DeductionAmount", precision = 10, scale = 2, nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal deductionAmount = BigDecimal.ZERO;

    
    @Column(name = "Month")
    private Integer month;

    
    @Column(name = "Year")
    private Integer year;
}