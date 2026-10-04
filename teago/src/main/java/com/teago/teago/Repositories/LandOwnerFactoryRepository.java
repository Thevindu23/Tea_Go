package com.teago.teago.Repositories;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.LandOwnerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LandOwnerFactoryRepository extends JpaRepository<LandOwnerFactory, Integer> {

    // All factory registrations for a specific land owner
    List<LandOwnerFactory> findByLandOwner(LandOwner landOwner);

    // All land owners registered with a specific factory
    List<LandOwnerFactory> findByFactory(FactoryOwner factory);

    // All active registrations for a land owner
    List<LandOwnerFactory> findByLandOwnerAndStatus(LandOwner landOwner,
                                                      LandOwnerFactory.Status status);

    // All registrations with a given status under a factory
    List<LandOwnerFactory> findByFactoryAndStatus(FactoryOwner factory,
                                                    LandOwnerFactory.Status status);

    // Check if a land owner is already registered with a factory
    Optional<LandOwnerFactory> findByLandOwnerAndFactory(LandOwner landOwner,
                                                           FactoryOwner factory);
}