package com.teago.teago.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Salary")
@Getter
@Setter
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SalaryID")
    private Integer salaryId;

    @ManyToOne
    @JoinColumn(name = "EmployeeID")
    private Employee employee;

    @Column(name = "TotalSalary", precision = 10, scale = 2)
    private BigDecimal totalSalary;

    @Column(name = "Month")
    private Integer month;

    @Column(name = "Year")
    private Integer year;
}
