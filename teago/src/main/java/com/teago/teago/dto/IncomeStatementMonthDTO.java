package com.teago.teago.dto;

import java.math.BigDecimal;

public class IncomeStatementMonthDTO {

    private Integer month;
    private Integer year;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPayable;

    public IncomeStatementMonthDTO(Integer month,
                                   Integer year,
                                   BigDecimal totalEarnings,
                                   BigDecimal totalDeductions,
                                   BigDecimal netPayable) {
        this.month = month;
        this.year = year;
        this.totalEarnings = totalEarnings;
        this.totalDeductions = totalDeductions;
        this.netPayable = netPayable;
    }

    public Integer getMonth() { return month; }
    public Integer getYear() { return year; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public BigDecimal getTotalDeductions() { return totalDeductions; }
    public BigDecimal getNetPayable() { return netPayable; }
}
