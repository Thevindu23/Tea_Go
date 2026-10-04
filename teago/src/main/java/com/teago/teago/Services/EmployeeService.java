package com.teago.teago.Services;

import com.teago.teago.models.Employee;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.Salary;
import com.teago.teago.Repositories.EmployeeRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.SalaryRepository;
import com.teago.teago.dto.EmployeeRequestDTO;
import com.teago.teago.dto.EmployeeResponseDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@org.springframework.transaction.annotation.Transactional
public class EmployeeService {

    private final EmployeeRepository  employeeRepository;
    private final LandOwnerRepository landOwnerRepository;
    private final SalaryRepository    salaryRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           LandOwnerRepository landOwnerRepository,
                           SalaryRepository salaryRepository) {
        this.employeeRepository  = employeeRepository;
        this.landOwnerRepository = landOwnerRepository;
        this.salaryRepository    = salaryRepository;
    }

    
    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO request) {
        LandOwner landOwner = landOwnerRepository.findById(request.getLandOwnerId())
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        Employee employee = new Employee();
        employee.setLandOwner(landOwner);
        employee.setEmployeeName(request.getEmployeeName());
        employee.setWorkDays(request.getWorkDays());
        employee.setSalaryRate(request.getSalaryRate());
        employeeRepository.save(employee);

        
        BigDecimal totalSalary = calculateSalary(request.getWorkDays(),
                request.getSalaryRate());
        autoCreateSalary(employee, totalSalary);

        return toDTO(employee, totalSalary);
    }


    public EmployeeResponseDTO updateEmployee(Integer employeeId,
                                              EmployeeRequestDTO request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: "
                        + employeeId));

        employee.setEmployeeName(request.getEmployeeName());
        employee.setWorkDays(request.getWorkDays());
        employee.setSalaryRate(request.getSalaryRate());
        employeeRepository.save(employee);

        BigDecimal totalSalary = calculateSalary(request.getWorkDays(),
                request.getSalaryRate());
        autoCreateSalary(employee, totalSalary);
        return toDTO(employee, totalSalary);
    }

    

    public List<EmployeeResponseDTO> getEmployeesByLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return employeeRepository.findByLandOwner(landOwner).stream()
                .map(e -> toDTO(e, calculateSalary(e.getWorkDays(), e.getSalaryRate())))
                .collect(Collectors.toList());
    }

   

    public void deleteEmployee(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: "
                        + employeeId));
        employeeRepository.delete(employee);
    }


    private BigDecimal calculateSalary(Integer workDays, BigDecimal salaryRate) {
        return salaryRate.multiply(BigDecimal.valueOf(workDays));
    }

    private void autoCreateSalary(Employee employee, BigDecimal totalSalary) {
        LocalDate now = LocalDate.now();
        
        salaryRepository.findByEmployeeAndMonthAndYear(
                employee, now.getMonthValue(), now.getYear())
                .ifPresentOrElse(
                        existing -> {
                            existing.setTotalSalary(totalSalary);
                            salaryRepository.save(existing);
                        },
                        () -> {
                            Salary salary = new Salary();
                            salary.setEmployee(employee);
                            salary.setTotalSalary(totalSalary);
                            salary.setMonth(now.getMonthValue());
                            salary.setYear(now.getYear());
                            salaryRepository.save(salary);
                        }
                );
    }

    private EmployeeResponseDTO toDTO(Employee e, BigDecimal totalSalary) {
        return new EmployeeResponseDTO(
                e.getEmployeeId(),
                e.getLandOwner().getLandOwnerId(),
                e.getEmployeeName(),
                e.getWorkDays(),
                e.getSalaryRate(),
                totalSalary
        );
    }
}