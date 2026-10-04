package com.teago.teago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class EmployeeRequestDTO {

    @NotNull(message = "Land owner ID is required")
    private Integer landOwnerId;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotNull(message = "Work days is required")
    @Min(value = 1, message = "Work days must be at least 1")
    private Integer workDays;

    @NotNull(message = "Salary rate is required")
    @DecimalMin(value = "1.00", message = "Salary rate must be greater than zero")
    private BigDecimal salaryRate;


    public Integer    getLandOwnerId()   { return landOwnerId; }
    public String     getEmployeeName()  { return employeeName; }
    public Integer    getWorkDays()      { return workDays; }
    public BigDecimal getSalaryRate()    { return salaryRate; }

    

    public void setLandOwnerId(Integer landOwnerId)   { this.landOwnerId = landOwnerId; }
    public void setEmployeeName(String employeeName)  { this.employeeName = employeeName; }
    public void setWorkDays(Integer workDays)         { this.workDays = workDays; }
    public void setSalaryRate(BigDecimal salaryRate)  { this.salaryRate = salaryRate; }
}