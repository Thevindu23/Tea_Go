package com.teago.teago.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"EmployeeID","EntryDate"}))
public class TeaCollection {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer collectionId;
    @ManyToOne @JoinColumn(name="EmployeeID",nullable=false) private Employee employee;
    @Column(name="EntryDate",nullable=false) private LocalDate collectionDate;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal quantityKg;
    @Column(length=500) private String notes;
    private String quality;
}
