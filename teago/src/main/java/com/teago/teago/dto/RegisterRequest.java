package com.teago.teago.dto;

import java.math.BigDecimal;

public class RegisterRequest {

    
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max=100)
    private String name;
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max=12)
    private String nic;
    private String address;
    private String contactNumber;
    @jakarta.validation.constraints.Pattern(regexp="[A-Za-z0-9_.-]{3,50}")
    @jakarta.validation.constraints.NotBlank
    private String username;
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(min=8, max=72)
    private String password;
    @jakarta.validation.constraints.NotBlank
    private String role;

    
    private String     nameWithInitials;
    private String     fullName;
    private String     dateOfBirth;        
    private BigDecimal landSize;
    private String     landLocation;

   
    private String     bankAccountName;
    private String     bankName;
    private String     bankBranch;
    private String     bankAccountNumber;

    
    private String     emergencyContactName;
    private String     emergencyContactAddress;
    private String     emergencyContactPhone;
    private String     emergencyContactRelationship;

    
    private String     factoryOwnerFullName;      
    private String     factoryName;
    private String     factoryLocation;
    private String     registrationNumber;        

    
    public String     getName()                          { return name; }
    public String     getNic()                           { return nic; }
    public String     getAddress()                       { return address; }
    public String     getContactNumber()                 { return contactNumber; }
    public String     getUsername()                      { return username; }
    public String     getPassword()                      { return password; }
    public String     getRole()                          { return role; }
    public String     getNameWithInitials()              { return nameWithInitials; }
    public String     getFullName()                      { return fullName; }
    public String     getDateOfBirth()                   { return dateOfBirth; }
    public BigDecimal getLandSize()                      { return landSize; }
    public String     getLandLocation()                  { return landLocation; }
    public String     getBankAccountName()               { return bankAccountName; }
    public String     getBankName()                      { return bankName; }
    public String     getBankBranch()                    { return bankBranch; }
    public String     getBankAccountNumber()             { return bankAccountNumber; }
    public String     getEmergencyContactName()          { return emergencyContactName; }
    public String     getEmergencyContactAddress()       { return emergencyContactAddress; }
    public String     getEmergencyContactPhone()         { return emergencyContactPhone; }
    public String     getEmergencyContactRelationship()  { return emergencyContactRelationship; }
    public String     getFactoryOwnerFullName()          { return factoryOwnerFullName; }
    public String     getFactoryName()                   { return factoryName; }
    public String     getFactoryLocation()               { return factoryLocation; }
    public String     getRegistrationNumber()            { return registrationNumber; }

    
    public void setName(String v)                          { this.name = v; }
    public void setNic(String v)                           { this.nic = v; }
    public void setAddress(String v)                       { this.address = v; }
    public void setContactNumber(String v)                 { this.contactNumber = v; }
    public void setUsername(String v)                      { this.username = v; }
    public void setPassword(String v)                      { this.password = v; }
    public void setRole(String v)                          { this.role = v; }
    public void setNameWithInitials(String v)              { this.nameWithInitials = v; }
    public void setFullName(String v)                      { this.fullName = v; }
    public void setDateOfBirth(String v)                   { this.dateOfBirth = v; }
    public void setLandSize(BigDecimal v)                  { this.landSize = v; }
    public void setLandLocation(String v)                  { this.landLocation = v; }
    public void setBankAccountName(String v)               { this.bankAccountName = v; }
    public void setBankName(String v)                      { this.bankName = v; }
    public void setBankBranch(String v)                    { this.bankBranch = v; }
    public void setBankAccountNumber(String v)             { this.bankAccountNumber = v; }
    public void setEmergencyContactName(String v)          { this.emergencyContactName = v; }
    public void setEmergencyContactAddress(String v)       { this.emergencyContactAddress = v; }
    public void setEmergencyContactPhone(String v)         { this.emergencyContactPhone = v; }
    public void setEmergencyContactRelationship(String v)  { this.emergencyContactRelationship = v; }
    public void setFactoryOwnerFullName(String v)          { this.factoryOwnerFullName = v; }
    public void setFactoryName(String v)                   { this.factoryName = v; }
    public void setFactoryLocation(String v)               { this.factoryLocation = v; }
    public void setRegistrationNumber(String v)            { this.registrationNumber = v; }

    
    public String getFullNameAlias()     { return this.name; }
    public void   setFullNameAlias(String v) { this.name = v; }
}