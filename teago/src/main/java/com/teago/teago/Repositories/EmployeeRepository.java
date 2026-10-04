package com.teago.teago.Repositories;

import com.teago.teago.models.Employee;
import com.teago.teago.models.LandOwner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    
    List<Employee> findByLandOwner(LandOwner landOwner);
}