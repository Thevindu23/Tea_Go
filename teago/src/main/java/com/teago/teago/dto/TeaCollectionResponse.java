package com.teago.teago.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TeaCollectionResponse(Integer collectionId, Integer employeeId, String employeeName, LocalDate collectionDate, BigDecimal quantityKg, String quality, String notes) {}
