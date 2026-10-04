package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "TeaRate")
@Getter
@Setter
public class TeaRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RateID")
    private Integer rateId;

    
    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID", nullable = false)
    private FactoryOwner factoryOwner;

    
    @Column(name = "RatePerKg", precision = 8, scale = 2, nullable = false)
    private BigDecimal ratePerKg;

    
    @Column(name = "EffectiveFrom", nullable = false)
    private LocalDate effectiveFrom;
}