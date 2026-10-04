package com.teago.teago.Repositories;

import com.teago.teago.models.Delivery;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {

    

    
    List<Delivery> findByLandOwner(LandOwner landOwner);

    
    List<Delivery> findByLandOwnerAndMonthAndYear(LandOwner landOwner,
                                                   Integer month, Integer year);

   

    
    List<Delivery> findByFactoryOwner(FactoryOwner factoryOwner);

    
    List<Delivery> findByFactoryOwnerAndLandOwner(FactoryOwner factoryOwner,
                                                   LandOwner landOwner);

    
    List<Delivery> findByFactoryOwnerAndMonthAndYear(FactoryOwner factoryOwner,
                                                      Integer month, Integer year);

    
    List<Delivery> findByFactoryOwnerAndLandOwnerAndMonthAndYear(FactoryOwner factoryOwner,
                                                                  LandOwner landOwner,
                                                                  Integer month, Integer year);
}