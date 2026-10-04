package com.teago.teago.controllers;
import com.teago.teago.Services.FieldRecordService;
import com.teago.teago.dto.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class FieldRecordController {
    private final FieldRecordService records;
    @PostMapping("/daily-wages") @PreAuthorize("@ownership.employee(#request.employeeId())")
    public DailyWageResponse saveWage(@Valid @RequestBody DailyWageRequest request) { return records.saveWage(request); }
    @GetMapping("/daily-wages/owner/{ownerId}/month") @PreAuthorize("@ownership.owner(#ownerId)")
    public List<DailyWageResponse> wages(@PathVariable Integer ownerId,@RequestParam Integer month,@RequestParam Integer year) { return records.wages(ownerId,month,year); }
    @PostMapping("/tea-collections") @PreAuthorize("@ownership.employee(#request.employeeId())")
    public TeaCollectionResponse saveCollection(@Valid @RequestBody TeaCollectionRequest request) { return records.saveCollection(request); }
    @GetMapping("/tea-collections/owner/{ownerId}") @PreAuthorize("@ownership.owner(#ownerId)")
    public List<TeaCollectionResponse> collections(@PathVariable Integer ownerId) { return records.collections(ownerId,null,null); }
    @GetMapping("/tea-collections/owner/{ownerId}/date-range") @PreAuthorize("@ownership.owner(#ownerId)")
    public List<TeaCollectionResponse> range(@PathVariable Integer ownerId,@RequestParam LocalDate startDate,@RequestParam LocalDate endDate) { return records.collections(ownerId,startDate,endDate); }
    @DeleteMapping("/tea-collections/{id}") @PreAuthorize("hasRole('LAND_OWNER')")
    public Map<String,String> delete(@PathVariable Integer id) { records.deleteCollection(id); return Map.of("message","Collection deleted"); }
}
