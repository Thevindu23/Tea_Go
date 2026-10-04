package com.teago.teago.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class UserDTO {

    private String name;
    private String nic;
    private String address;
    private String contactNumber;
    private String username;
    private String password;
    private String role; // LandOwner, FactoryOwner, Supervisor

    public String getName() { return name; }
    public String getNic() { return nic; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setNic(String nic) { this.nic = nic; }
    public void setAddress(String address) { this.address = address; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
