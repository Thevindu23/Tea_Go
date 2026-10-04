package com.teago.teago.Repositories;

import com.teago.teago.models.FertilizerRequest;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FertilizerRequestRepository extends JpaRepository<FertilizerRequest, Integer> {

    
    List<FertilizerRequest> findByLandOwner(LandOwner landOwner);

    
    List<FertilizerRequest> findByApprovalStatus(FertilizerRequest.ApprovalStatus approvalStatus);

    
    List<FertilizerRequest> findByFactoryOwner(FactoryOwner factoryOwner);

    
    List<FertilizerRequest> findByFactoryOwnerAndApprovalStatus(
            FactoryOwner factoryOwner,
            FertilizerRequest.ApprovalStatus approvalStatus);
}