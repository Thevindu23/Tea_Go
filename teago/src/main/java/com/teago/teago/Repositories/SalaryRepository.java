package com.teago.teago.Repositories;

import com.teago.teago.models.Employee;
import com.teago.teago.models.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary, Integer> {


    List<Salary> findByEmployee(Employee employee);

    
    Optional<Salary> findByEmployeeAndMonthAndYear(Employee employee,
                                                    Integer month, Integer year);
}