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
@Table(name = "Complaint")
@Getter
@Setter
public class Complaint {
    @jakarta.persistence.Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComplaintID")
    private Integer complaintId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID")
    private LandOwner landOwner;

    @ManyToOne
    @JoinColumn(name = "FactoryOwnerID")
    private FactoryOwner factoryOwner;

    @Column(name = "ComplaintText", columnDefinition = "TEXT")
    private String complaintText;

    @Column(name = "ResponseText", columnDefinition = "TEXT")
    private String responseText;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private Status status;

    @Column(name = "DateSubmitted")
    private LocalDate dateSubmitted;

    public enum Status {
        Pending, Resolved, Rejected
    }
}

