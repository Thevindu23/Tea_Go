package com.teago.teago.controllers;

import com.teago.teago.Services.MonthlyBillService;
import com.teago.teago.dto.IncomeStatementResponseDTO;
import com.teago.teago.dto.MonthlyBillResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/bills")
public class MonthlyBillController {

    private final MonthlyBillService billService;

    public MonthlyBillController(MonthlyBillService billService) {
        this.billService = billService;
    }

    
    @GetMapping("/preview/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> preview(@PathVariable Integer landOwnerId, @RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(billService.preview(landOwnerId, 0, month, year));
    }

    @PostMapping("/generate/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> generateBill(
            @PathVariable Integer landOwnerId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        try {
            MonthlyBillResponseDTO response =
                    billService.generateBill(landOwnerId, month, year);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @PostMapping("/factory/{factoryOwnerId}/generate/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> generateBillByFactoryOwner(
            @PathVariable Integer factoryOwnerId,
            @PathVariable Integer landOwnerId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        try {
            MonthlyBillResponseDTO response =
                    billService.generateBillForFactoryOwner(factoryOwnerId, landOwnerId, month, year);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @PostMapping("/factory/{factoryOwnerId}/generate")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> generateBillsForFactoryOwner(
            @PathVariable Integer factoryOwnerId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        try {
            List<MonthlyBillResponseDTO> response =
                    billService.generateBillsForFactoryOwner(factoryOwnerId, month, year);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getBillsByOwner(@PathVariable Integer landOwnerId) {
        try {
            List<MonthlyBillResponseDTO> list =
                    billService.getBillsByLandOwner(landOwnerId);
            return ResponseEntity.ok(list);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/factory/{factoryOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> getBillsByFactoryOwner(@PathVariable Integer factoryOwnerId) {
        try {
            List<MonthlyBillResponseDTO> list =
                    billService.getBillsByFactoryOwner(factoryOwnerId);
            return ResponseEntity.ok(list);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/statement/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> generateIncomeStatementByOwner(
            @PathVariable Integer landOwnerId,
            @RequestParam Integer months,
            @RequestParam Integer endMonth,
            @RequestParam Integer endYear) {
        try {
            IncomeStatementResponseDTO response = billService.generateIncomeStatement(
                    landOwnerId, months, endMonth, endYear
            );
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @GetMapping("/statement/factory/{factoryOwnerId}/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.factory(#factoryOwnerId)")
    public ResponseEntity<?> generateIncomeStatementByFactoryOwner(
            @PathVariable Integer factoryOwnerId,
            @PathVariable Integer landOwnerId,
            @RequestParam Integer months,
            @RequestParam Integer endMonth,
            @RequestParam Integer endYear) {
        try {
            IncomeStatementResponseDTO response = billService.generateIncomeStatementForFactoryOwner(
                    factoryOwnerId, landOwnerId, months, endMonth, endYear
            );
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}