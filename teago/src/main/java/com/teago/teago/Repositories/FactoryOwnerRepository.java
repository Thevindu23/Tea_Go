package com.teago.teago.Repositories;

import com.teago.teago.models.FactoryOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactoryOwnerRepository extends JpaRepository<FactoryOwner, Integer> {

    
    Optional<FactoryOwner> findByUser_UserId(Integer userId);
}