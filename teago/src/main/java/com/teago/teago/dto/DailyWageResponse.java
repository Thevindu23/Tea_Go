package com.teago.teago.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record DailyWageResponse(Integer wageId, Integer employeeId, String employeeName, LocalDate wageDate, BigDecimal dailyRate, String notes) {}
