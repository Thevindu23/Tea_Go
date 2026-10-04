package com.teago.teago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public class FactoryRegistrationResponseDTO {

    private Integer    registrationId;
    private Integer    landOwnerId;
    private String     landOwnerName;
    private String     landOwnerNic;
    private Integer    factoryId;
    private String     factoryName;
    private String     factoryLocation;
    private String     factoryOwnerName;
    private String     factoryOwnerContact;
    private BigDecimal landSize;
    private String     status;          
    private LocalDate  registrationDate;

    

    public FactoryRegistrationResponseDTO(Integer registrationId,
                                           Integer landOwnerId, String landOwnerName,
                                           String landOwnerNic,
                                           Integer factoryId, String factoryName,
                                           String factoryLocation,
                                           String factoryOwnerName,
                                           String factoryOwnerContact,
                                           BigDecimal landSize,
                                           String status, LocalDate registrationDate) {
        this.registrationId   = registrationId;
        this.landOwnerId      = landOwnerId;
        this.landOwnerName    = landOwnerName;
        this.landOwnerNic     = landOwnerNic;
        this.factoryId        = factoryId;
        this.factoryName      = factoryName;
        this.factoryLocation  = factoryLocation;
        this.factoryOwnerName = factoryOwnerName;
        this.factoryOwnerContact = factoryOwnerContact;
        this.landSize         = landSize;
        this.status           = status;
        this.registrationDate = registrationDate;
    }

    

    public Integer    getRegistrationId()   { return registrationId; }
    public Integer    getLandOwnerId()      { return landOwnerId; }
    public String     getLandOwnerName()    { return landOwnerName; }
    public String     getLandOwnerNic()     { return landOwnerNic; }
    public Integer    getFactoryId()        { return factoryId; }
    public String     getFactoryName()      { return factoryName; }
    public String     getFactoryLocation()  { return factoryLocation; }
    public String     getFactoryOwnerName() { return factoryOwnerName; }
    public String     getFactoryOwnerContact() { return factoryOwnerContact; }
    public BigDecimal getLandSize()         { return landSize; }
    public String     getStatus()           { return status; }
    public LocalDate  getRegistrationDate() { return registrationDate; }
}