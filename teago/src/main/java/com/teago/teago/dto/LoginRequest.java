package com.teago.teago.dto;

public class LoginRequest {

    @jakarta.validation.constraints.NotBlank
    private String username;
    @jakarta.validation.constraints.NotBlank
    private String password;

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
