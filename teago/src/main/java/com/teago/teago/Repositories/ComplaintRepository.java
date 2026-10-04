package com.teago.teago.Repositories;

import com.teago.teago.models.Complaint;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    
    List<Complaint> findByLandOwner(LandOwner landOwner);

    List<Complaint> findByStatus(Complaint.Status status);

    
    List<Complaint> findByFactoryOwner(FactoryOwner factoryOwner);

    List<Complaint> findByFactoryOwnerAndStatus(FactoryOwner factoryOwner, Complaint.Status status);
}