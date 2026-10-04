package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class MonthlyBillResponseDTO {

    private Integer    billId;
    private Integer    landOwnerId;
    private String     landOwnerName;
    private Integer    month;
    private Integer    year;
    private BigDecimal totalEarnings;
    private BigDecimal deliveryDeductions;
    private BigDecimal loanDeductions;
    private BigDecimal fertilizerDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netPayable;
    private LocalDate  generatedDate;

    

    public MonthlyBillResponseDTO(Integer billId, Integer landOwnerId,
                                  String landOwnerName, Integer month, Integer year,
                                  BigDecimal totalEarnings, BigDecimal deliveryDeductions,
                                  BigDecimal loanDeductions,
                                  BigDecimal fertilizerDeductions,
                                  BigDecimal totalDeductions, BigDecimal netPayable,
                                  LocalDate generatedDate) {
        this.billId               = billId;
        this.landOwnerId          = landOwnerId;
        this.landOwnerName        = landOwnerName;
        this.month                = month;
        this.year                 = year;
        this.totalEarnings        = totalEarnings;
        this.deliveryDeductions   = deliveryDeductions;
        this.loanDeductions       = loanDeductions;
        this.fertilizerDeductions = fertilizerDeductions;
        this.totalDeductions      = totalDeductions;
        this.netPayable           = netPayable;
        this.generatedDate        = generatedDate;
    }

    
    public Integer    getBillId()               { return billId; }
    public Integer    getLandOwnerId()          { return landOwnerId; }
    public String     getLandOwnerName()        { return landOwnerName; }
    public Integer    getMonth()                { return month; }
    public Integer    getYear()                 { return year; }
    public BigDecimal getTotalEarnings()        { return totalEarnings; }
    public BigDecimal getDeliveryDeductions()   { return deliveryDeductions; }
    public BigDecimal getLoanDeductions()       { return loanDeductions; }
    public BigDecimal getFertilizerDeductions() { return fertilizerDeductions; }
    public BigDecimal getTotalDeductions()      { return totalDeductions; }
    public BigDecimal getNetPayable()           { return netPayable; }
    public LocalDate  getGeneratedDate()        { return generatedDate; }
}