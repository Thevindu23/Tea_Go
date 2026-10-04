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
@Table(name = "Employee")
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmployeeID")
    private Integer employeeId;

    @ManyToOne
    @JoinColumn(name = "LandOwnerID")
    private LandOwner landOwner;

    @Column(name = "EmployeeName", length = 100)
    private String employeeName;

    @Column(name = "WorkDays")
    private Integer workDays;

    @Column(name = "SalaryRate", precision = 8, scale = 2)
    private BigDecimal salaryRate;
}
