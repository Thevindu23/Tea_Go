package com.teago.teago.Repositories;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    List<Loan> findByLandOwner(LandOwner landOwner);

    List<Loan> findByApprovalStatus(Loan.ApprovalStatus approvalStatus);

   
    List<Loan> findByLandOwnerAndApprovalStatus(LandOwner landOwner,
                                                 Loan.ApprovalStatus approvalStatus);

    List<Loan> findByFactoryOwner(FactoryOwner factoryOwner);

    List<Loan> findByFactoryOwnerAndApprovalStatus(FactoryOwner factoryOwner,
                                                   Loan.ApprovalStatus approvalStatus);
}