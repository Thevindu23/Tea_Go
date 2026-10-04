package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "LandOwner")
@Getter
@Setter
public class LandOwner {

    @Id
    @Column(name = "LandOwnerID")
    private Integer landOwnerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "LandOwnerID")
    private User user;

    

    @Column(name = "NameWithInitials", length = 150)
    private String nameWithInitials;        // e.g. "K.A. Perera"

    @Column(name = "FullName", length = 150)
    private String fullName;                // e.g. "Kamal Asanka Perera"

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    

    @Column(name = "LandSize", precision = 10, scale = 2)
    private BigDecimal landSize;

    @Column(name = "LandLocation", length = 255)
    private String landLocation;

    

    @Column(name = "BankAccountName", length = 150)
    private String bankAccountName;

    @Column(name = "BankName", length = 100)
    private String bankName;

    @Column(name = "BankBranch", length = 100)
    private String bankBranch;

    @Column(name = "BankAccountNumber", length = 50)
    private String bankAccountNumber;

    

    @Column(name = "EmergencyContactName", length = 150)
    private String emergencyContactName;

    @Column(name = "EmergencyContactAddress", length = 255)
    private String emergencyContactAddress;

    @Column(name = "EmergencyContactPhone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "EmergencyContactRelationship", length = 100)
    private String emergencyContactRelationship;

    

    @OneToMany(mappedBy = "landOwner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LandOwnerFactory> factoryRegistrations = new ArrayList<>();
}