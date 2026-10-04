package com.teago.teago.Repositories;

import com.teago.teago.models.LandOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LandOwnerRepository extends JpaRepository<LandOwner, Integer> {

    
    @org.springframework.data.jpa.repository.Query(value="select * from land_owner where land_ownerid = :id for update", nativeQuery=true)
    Optional<LandOwner> lockById(@org.springframework.data.repository.query.Param("id") Integer id);

    Optional<LandOwner> findByUser_UserId(Integer userId);

    
}
