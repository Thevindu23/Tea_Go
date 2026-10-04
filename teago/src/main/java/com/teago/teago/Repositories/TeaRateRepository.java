package com.teago.teago.Repositories;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.TeaRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeaRateRepository extends JpaRepository<TeaRate, Integer> {

    
    List<TeaRate> findByFactoryOwnerOrderByEffectiveFromDesc(FactoryOwner factoryOwner);

    
    Optional<TeaRate> findFirstByFactoryOwnerAndEffectiveFromLessThanEqualOrderByEffectiveFromDescRateIdDesc(FactoryOwner factoryOwner, java.time.LocalDate date);
    Optional<TeaRate> findFirstByFactoryOwnerOrderByEffectiveFromDesc(FactoryOwner factoryOwner);
}