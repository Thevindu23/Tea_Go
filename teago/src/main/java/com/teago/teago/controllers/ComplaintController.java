package com.teago.teago.controllers;

import com.teago.teago.Services.ComplaintService;
import com.teago.teago.dto.ComplaintRequestDTO;
import com.teago.teago.dto.ComplaintResponseDTO;
import com.teago.teago.dto.ComplaintResponseUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


 
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#request.landOwnerId)")
    public ResponseEntity<?> submitComplaint(
            @Valid @RequestBody ComplaintRequestDTO request) {
        try {
            ComplaintResponseDTO response = complaintService.submitComplaint(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<List<ComplaintResponseDTO>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    
    @GetMapping("/pending")
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<List<ComplaintResponseDTO>> getPendingComplaints() {
        return ResponseEntity.ok(complaintService.getPendingComplaints());
    }

    @GetMapping("/pending/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getPendingComplaintsByFactoryOwner(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(complaintService.getPendingComplaintsByFactoryOwner(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/factory/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getComplaintsByFactoryOwner(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(complaintService.getComplaintsByFactoryOwner(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getComplaintsByOwner(@PathVariable Integer landOwnerId) {
        try {
            return ResponseEntity.ok(
                    complaintService.getComplaintsByLandOwner(landOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{complaintId}/response")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.complaint(#complaintId)")
    public ResponseEntity<?> respondToComplaint(
            @PathVariable Integer complaintId,
            @Valid @RequestBody ComplaintResponseUpdateDTO request) {
        try {
            ComplaintResponseDTO response =
                    complaintService.respondToComplaint(complaintId, request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}