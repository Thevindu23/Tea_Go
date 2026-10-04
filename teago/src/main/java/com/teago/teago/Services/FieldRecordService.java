package com.teago.teago.Services;
import com.teago.teago.Repositories.*;
import com.teago.teago.models.*;
import com.teago.teago.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class FieldRecordService {
    private final EmployeeRepository employees;
    private final DailyWageRepository wages;
    private final TeaCollectionRepository collections;
    private final LandOwnerRepository owners;
    private final com.teago.teago.config.Ownership ownership;
    @Transactional public DailyWageResponse saveWage(DailyWageRequest r) {
        Employee e=employee(r.employeeId()); owners.lockById(e.getLandOwner().getLandOwnerId()).orElseThrow();
        DailyWage w=wages.findByEmployeeAndWageDate(e,r.wageDate()).orElseGet(DailyWage::new);
        w.setEmployee(e); w.setWageDate(r.wageDate()); w.setDailyRate(r.dailyRate()); w.setNotes(r.notes());
        return wage(wages.save(w));
    }
    @Transactional public TeaCollectionResponse saveCollection(TeaCollectionRequest r) {
        Employee e=employee(r.employeeId()); owners.lockById(e.getLandOwner().getLandOwnerId()).orElseThrow();
        TeaCollection c=collections.findByEmployeeAndCollectionDate(e,r.collectionDate()).orElseGet(TeaCollection::new);
        c.setEmployee(e); c.setCollectionDate(r.collectionDate()); c.setQuantityKg(r.quantityKg()); c.setQuality(r.quality()); c.setNotes(r.notes());
        return collection(collections.save(c));
    }
    public List<DailyWageResponse> wages(Integer owner,Integer month,Integer year) {
        YearMonth target=YearMonth.of(year,month);
        return wages.findByEmployee_LandOwner_LandOwnerIdAndWageDateBetweenOrderByWageDateAsc(owner,target.atDay(1),target.atEndOfMonth()).stream().map(this::wage).toList();
    }
    public List<TeaCollectionResponse> collections(Integer owner,LocalDate start,LocalDate end) {
        if(start==null && end==null) return collections.findByEmployee_LandOwner_LandOwnerIdOrderByCollectionDateDesc(owner).stream().map(this::collection).toList();
        if(start==null || end==null || start.isAfter(end)) throw new IllegalArgumentException("Invalid date range");
        return collections.findByEmployee_LandOwner_LandOwnerIdAndCollectionDateBetweenOrderByCollectionDateAsc(owner,start,end).stream().map(this::collection).toList();
    }
    @Transactional public void deleteCollection(Integer id) {
        TeaCollection c=collections.findById(id).orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if(!ownership.employee(c.getEmployee().getEmployeeId())) throw new org.springframework.security.access.AccessDeniedException("Access denied");
        collections.delete(c);
    }
    private Employee employee(Integer id) { return employees.findById(id).orElseThrow(() -> new IllegalArgumentException("Employee not found")); }
    private DailyWageResponse wage(DailyWage w) { return new DailyWageResponse(w.getWageId(),w.getEmployee().getEmployeeId(),w.getEmployee().getEmployeeName(),w.getWageDate(),w.getDailyRate(),w.getNotes()); }
    private TeaCollectionResponse collection(TeaCollection c) { return new TeaCollectionResponse(c.getCollectionId(),c.getEmployee().getEmployeeId(),c.getEmployee().getEmployeeName(),c.getCollectionDate(),c.getQuantityKg(),c.getQuality(),c.getNotes()); }
}
