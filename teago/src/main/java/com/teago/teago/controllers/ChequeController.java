package com.teago.teago.controllers;

import com.teago.teago.Services.ChequeService;
import com.teago.teago.dto.ChequeIssueRequestDTO;
import com.teago.teago.dto.ChequeResponseDTO;
import com.teago.teago.dto.ChequeStatusUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cheques")
public class ChequeController {

    private final ChequeService chequeService;

    public ChequeController(ChequeService chequeService) {
        this.chequeService = chequeService;
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.loan(#request.loanId)")
    public ResponseEntity<?> issueCheque(
            @Valid @RequestBody ChequeIssueRequestDTO request) {
        try {
            ChequeResponseDTO response = chequeService.issueCheque(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

   
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<List<ChequeResponseDTO>> getAllCheques() {
        return ResponseEntity.ok(chequeService.getAllCheques());
    }

    
    @GetMapping("/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getChequesByOwner(@PathVariable Integer landOwnerId) {
        try {
            return ResponseEntity.ok(
                    chequeService.getChequesByLandOwner(landOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

   
    @GetMapping("/factory/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getChequesByFactory(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(
                    chequeService.getChequesByFactoryOwner(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/supervisor/{supervisorId}")
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<?> getChequesBySupervisor(
            @PathVariable Integer supervisorId) {
        try {
            return ResponseEntity.ok(
                    chequeService.getChequesBySupervisor(supervisorId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

   
    @PutMapping("/{chequeId}/delivery-status")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.chequeFactory(#chequeId)")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Integer chequeId,
            @Valid @RequestBody ChequeStatusUpdateDTO request) {
        try {
            return ResponseEntity.ok(
                    chequeService.updateDeliveryStatus(chequeId, request));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{chequeId}/issue")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.chequeFactory(#chequeId)")
    public ResponseEntity<?> issueChequeAction(
            @PathVariable Integer chequeId) {
        try {
            return ResponseEntity.ok(
                    chequeService.markChequeAsIssued(chequeId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }


    @PutMapping("/{chequeId}/verification-status")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.chequeOwner(#chequeId)")
    public ResponseEntity<?> updateVerificationStatus(
            @PathVariable Integer chequeId,
            @Valid @RequestBody ChequeStatusUpdateDTO request) {
        try {
            return ResponseEntity.ok(
                    chequeService.updateVerificationStatus(chequeId, request));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
