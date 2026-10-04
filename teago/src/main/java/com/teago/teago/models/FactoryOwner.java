package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "FactoryOwner")
public class FactoryOwner {

    @Id
    @Column(name = "FactoryOwnerID")
    private Integer factoryOwnerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "FactoryOwnerID")
    private User user;

    /** Full name of the factory owner */
    @Column(name = "FullName", length = 150)
    private String fullName;

    /** Official factory / company name */
    @Column(name = "FactoryName", length = 150)
    private String factoryName;

    /** Physical location of the factory */
    @Column(name = "FactoryLocation", length = 255)
    private String factoryLocation;

    /** Official business / company registration number */
    @Column(name = "RegistrationNumber", length = 100, unique = true)
    private String registrationNumber;
}