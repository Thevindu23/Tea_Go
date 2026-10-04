package com.teago.teago.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"FactoryOwnerID","LandOwnerID","BillMonth","BillYear"}))
public class Settlement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne @JoinColumn(name="FactoryOwnerID",nullable=false) private FactoryOwner factory;
    @ManyToOne @JoinColumn(name="LandOwnerID",nullable=false) private LandOwner owner;
    @Column(name="BillMonth",nullable=false) private Integer month;
    @Column(name="BillYear",nullable=false) private Integer year;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
    private String method;
    private String reference;
    private LocalDate recordedDate;
}
