package com.teago.teago.controllers;

import com.teago.teago.Services.LoanService;
import com.teago.teago.dto.LoanRequestDTO;
import com.teago.teago.dto.LoanResponseDTO;
import com.teago.teago.dto.LoanStatusUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    
    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#request.landOwnerId)")
    public ResponseEntity<?> applyForLoan(@Valid @RequestBody LoanRequestDTO request) {
        try {
            LoanResponseDTO response = loanService.applyForLoan(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

  
    @GetMapping("/pending")
    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    public ResponseEntity<List<LoanResponseDTO>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }

   
    @GetMapping("/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getLoansByOwner(@PathVariable Integer landOwnerId) {
        try {
            return ResponseEntity.ok(loanService.getLoansByLandOwner(landOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

  
    @GetMapping("/factory/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getLoansByFactory(@PathVariable Integer factoryOwnerId) {
        try {
            return ResponseEntity.ok(loanService.getLoansByFactoryOwner(factoryOwnerId));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{loanId}/status")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.loan(#loanId)")
    public ResponseEntity<?> updateLoanStatus(@PathVariable Integer loanId,
                                               @Valid @RequestBody LoanStatusUpdateDTO request) {
        try {
            LoanResponseDTO response = loanService.updateLoanStatus(loanId, request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
