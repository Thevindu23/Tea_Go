package com.teago.teago.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
@Entity
@Table(name = "User")
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;
 
    @Column(name = "Name", length = 100)
    private String name;
 
    @Column(name = "NIC", length = 12, unique = true, nullable = false)
    private String nic;
 
    @Column(name = "Address", length = 255)
    private String address;
 
    @Column(name = "ContactNumber", length = 15)
    private String contactNumber;
 
    @Column(name = "Username", length = 50, unique = true, nullable = false)
    private String username;
 
    @Column(name = "Password", length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "Role")
    private Role role;
 
    // Back-reference so controllers can do user.getLandOwner()
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private LandOwner landOwner;
 
    // Back-reference so controllers can do user.getFactoryOwner()
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private FactoryOwner factoryOwner;
 
    public enum Role {
        LandOwner, FactoryOwner, Supervisor
    }
}