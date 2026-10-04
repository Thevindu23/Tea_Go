package com.teago.teago.dto;

import java.math.BigDecimal;


public class EmployeeResponseDTO {

    private Integer    employeeId;
    private Integer    landOwnerId;
    private String     employeeName;
    private Integer    workDays;
    private BigDecimal salaryRate;
    private BigDecimal totalSalary;  

    

    public EmployeeResponseDTO(Integer employeeId, Integer landOwnerId,
                               String employeeName, Integer workDays,
                               BigDecimal salaryRate, BigDecimal totalSalary) {
        this.employeeId   = employeeId;
        this.landOwnerId  = landOwnerId;
        this.employeeName = employeeName;
        this.workDays     = workDays;
        this.salaryRate   = salaryRate;
        this.totalSalary  = totalSalary;
    }

   

    public Integer    getEmployeeId()   { return employeeId; }
    public Integer    getLandOwnerId()  { return landOwnerId; }
    public String     getEmployeeName() { return employeeName; }
    public Integer    getWorkDays()     { return workDays; }
    public BigDecimal getSalaryRate()   { return salaryRate; }
    public BigDecimal getTotalSalary()  { return totalSalary; }
}