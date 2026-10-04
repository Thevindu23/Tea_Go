package com.teago.teago.controllers;

import com.teago.teago.Services.FactoryRegistrationService;
import com.teago.teago.dto.FactoryRegistrationRequestDTO;
import com.teago.teago.dto.FactoryRegistrationResponseDTO;
import com.teago.teago.dto.FactoryRegistrationStatusUpdateDTO;
import com.teago.teago.dto.LandOwnerDetailsDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/factories")
public class FactoryRegistrationController {

    private final FactoryRegistrationService service;

    public FactoryRegistrationController(FactoryRegistrationService service) {
        this.service = service;
    }

    
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('LAND_OWNER','FACTORY_OWNER')")
    public ResponseEntity<List<FactoryRegistrationResponseDTO>> getAllFactories() {
        return ResponseEntity.ok(service.getAllFactories());
    }

    
    @GetMapping("/registrations/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getMyFactories(@PathVariable Integer landOwnerId) {
        try {
            return ResponseEntity.ok(service.getMyFactories(landOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @PostMapping("/register")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#request.landOwnerId)")
    public ResponseEntity<?> register(
            @Valid @RequestBody FactoryRegistrationRequestDTO request) {
        try {
            FactoryRegistrationResponseDTO response = service.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/pending/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getPendingRegistrations(
            @PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(service.getPendingRegistrations(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

   
    @GetMapping("/owners/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getFactoryRegistrations(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(service.getFactoryRegistrations(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @PutMapping("/registrations/{registrationId}/status")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.registration(#registrationId)")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer registrationId,
            @Valid @RequestBody FactoryRegistrationStatusUpdateDTO request) {
        try {
            return ResponseEntity.ok(service.updateStatus(registrationId, request));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/active-owners/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getActiveLandOwners(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(service.getActiveLandOwners(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/{factoryOwnerId}/owner-details/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getLandOwnerDetails(
            @PathVariable Integer factoryOwnerId,
            @PathVariable Integer landOwnerId) {
        try {
            LandOwnerDetailsDTO details = service.getLandOwnerDetails(factoryOwnerId, landOwnerId);
            return ResponseEntity.ok(details);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}