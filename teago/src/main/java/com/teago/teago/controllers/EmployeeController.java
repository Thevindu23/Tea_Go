package com.teago.teago.controllers;

import com.teago.teago.Services.EmployeeService;
import com.teago.teago.dto.EmployeeRequestDTO;
import com.teago.teago.dto.EmployeeResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#request.landOwnerId)")
    public ResponseEntity<?> addEmployee(
            @Valid @RequestBody EmployeeRequestDTO request) {
        try {
            EmployeeResponseDTO response = employeeService.addEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/owner/{landOwnerId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.owner(#landOwnerId)")
    public ResponseEntity<?> getEmployeesByOwner(@PathVariable Integer landOwnerId) {
        try {
            List<EmployeeResponseDTO> list =
                    employeeService.getEmployeesByLandOwner(landOwnerId);
            return ResponseEntity.ok(list);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @PutMapping("/{employeeId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.employee(#employeeId) and @ownership.owner(#request.landOwnerId)")
    public ResponseEntity<?> updateEmployee(@PathVariable Integer employeeId,
                                             @Valid @RequestBody EmployeeRequestDTO request) {
        try {
            EmployeeResponseDTO response =
                    employeeService.updateEmployee(employeeId, request);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    
    @DeleteMapping("/{employeeId}")
    @org.springframework.security.access.prepost.PreAuthorize("@ownership.employee(#employeeId)")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
        } catch (org.springframework.dao.DataAccessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}