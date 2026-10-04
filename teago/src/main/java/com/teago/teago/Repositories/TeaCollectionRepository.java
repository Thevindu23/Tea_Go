package com.teago.teago.Repositories;
import com.teago.teago.models.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TeaCollectionRepository extends JpaRepository<TeaCollection,Integer> {
    Optional<TeaCollection> findByEmployeeAndCollectionDate(Employee employee,LocalDate date);
    List<TeaCollection> findByEmployee_LandOwner_LandOwnerIdOrderByCollectionDateDesc(Integer ownerId);
    List<TeaCollection> findByEmployee_LandOwner_LandOwnerIdAndCollectionDateBetweenOrderByCollectionDateAsc(Integer ownerId,LocalDate start,LocalDate end);
}
