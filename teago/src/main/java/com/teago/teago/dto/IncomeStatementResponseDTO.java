package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class IncomeStatementResponseDTO {

    private Integer landOwnerId;
    private String landOwnerName;
    private Integer periodMonths;
    private Integer endMonth;
    private Integer endYear;
    private List<IncomeStatementMonthDTO> months;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetPayable;
    private LocalDate generatedDate;

    public IncomeStatementResponseDTO(Integer landOwnerId,
                                      String landOwnerName,
                                      Integer periodMonths,
                                      Integer endMonth,
                                      Integer endYear,
                                      List<IncomeStatementMonthDTO> months,
                                      BigDecimal totalEarnings,
                                      BigDecimal totalDeductions,
                                      BigDecimal totalNetPayable,
                                      LocalDate generatedDate) {
        this.landOwnerId = landOwnerId;
        this.landOwnerName = landOwnerName;
        this.periodMonths = periodMonths;
        this.endMonth = endMonth;
        this.endYear = endYear;
        this.months = months;
        this.totalEarnings = totalEarnings;
        this.totalDeductions = totalDeductions;
        this.totalNetPayable = totalNetPayable;
        this.generatedDate = generatedDate;
    }

    public Integer getLandOwnerId() { return landOwnerId; }
    public String getLandOwnerName() { return landOwnerName; }
    public Integer getPeriodMonths() { return periodMonths; }
    public Integer getEndMonth() { return endMonth; }
    public Integer getEndYear() { return endYear; }
    public List<IncomeStatementMonthDTO> getMonths() { return months; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public BigDecimal getTotalNetPayable() { return totalNetPayable; }
    public LocalDate getGeneratedDate() { return generatedDate; }
}
