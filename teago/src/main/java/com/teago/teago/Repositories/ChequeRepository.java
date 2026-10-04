package com.teago.teago.Repositories;

import com.teago.teago.models.Cheque;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeRepository extends JpaRepository<Cheque, Integer> {

    
    Optional<Cheque> findByLoan(Loan loan);

    
    List<Cheque> findByLoan_LandOwner(LandOwner landOwner);

    
    List<Cheque> findByLoan_FactoryOwner(FactoryOwner factoryOwner);

    
    
    List<Cheque> findByDeliveryStatus(Cheque.DeliveryStatus deliveryStatus);
}