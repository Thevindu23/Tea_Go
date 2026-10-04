package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "LandOwnerFactory",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"LandOwnerID", "FactoryOwnerID"}))
@Getter
@Setter
public class LandOwnerFactory {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RegistrationID")
    private Integer registrationId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID", nullable = false)
    private LandOwner landOwner;

    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID", nullable = false)
    private FactoryOwner factory;

    /** Land size (in acres) the owner supplies to THIS factory. */
    @Column(name = "LandSize", precision = 10, scale = 2)
    private BigDecimal landSize;

    @Column(name = "RegistrationDate")
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private Status status;

    public enum Status {
        Pending, Active, Rejected
    }
}