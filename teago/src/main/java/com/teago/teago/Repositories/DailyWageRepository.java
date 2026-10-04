package com.teago.teago.Repositories;
import com.teago.teago.models.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DailyWageRepository extends JpaRepository<DailyWage,Integer> {
    Optional<DailyWage> findByEmployeeAndWageDate(Employee employee,LocalDate date);
    List<DailyWage> findByEmployee_LandOwner_LandOwnerIdOrderByWageDateDesc(Integer ownerId);
    List<DailyWage> findByEmployee_LandOwner_LandOwnerIdAndWageDateBetweenOrderByWageDateAsc(Integer ownerId,LocalDate start,LocalDate end);
}
