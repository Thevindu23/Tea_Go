package com.teago.teago.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teago.teago.models.User;
import com.teago.teago.Repositories.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('LAND_OWNER') or hasRole('FACTORY_OWNER') or hasRole('SUPERVISOR')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
            "userId", user.getUserId(),
            "name", user.getName(),
            "username", user.getUsername(),
            "nic", user.getNic(),
            "address", user.getAddress(),
            "contactNumber", user.getContactNumber(),
            "role", user.getRole().toString()
        ));
    }

    @GetMapping("/land-owner")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<?> getLandOwnerDashboard(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome to Land Owner Dashboard",
            "user", authentication.getName(),
            "role", "LAND_OWNER"
        ));
    }

    @GetMapping("/factory-owner")
    @PreAuthorize("hasRole('FACTORY_OWNER')")
    public ResponseEntity<?> getFactoryOwnerDashboard(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome to Factory Owner Dashboard",
            "user", authentication.getName(),
            "role", "FACTORY_OWNER"
        ));
    }

    @GetMapping("/supervisor")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> getSupervisorDashboard(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome to Supervisor Dashboard",
            "user", authentication.getName(),
            "role", "SUPERVISOR"
        ));
    }
}