package com.teago.teago.Services;

import com.teago.teago.models.FertilizerRequest;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.LandOwnerFactory;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.FertilizerRequestRepository;
import com.teago.teago.Repositories.LandOwnerFactoryRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.dto.FertilizerRequestDTO;
import com.teago.teago.dto.FertilizerResponseDTO;
import com.teago.teago.dto.FertilizerStatusUpdateDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@org.springframework.transaction.annotation.Transactional
public class FertilizerService {

    
    private static final BigDecimal UNIT_PRICE = BigDecimal.valueOf(40);

    private final FertilizerRequestRepository fertilizerRepository;
    private final LandOwnerRepository         landOwnerRepository;
    private final FactoryOwnerRepository      factoryOwnerRepository;
    private final LandOwnerFactoryRepository  landOwnerFactoryRepository;

    public FertilizerService(FertilizerRequestRepository fertilizerRepository,
                             LandOwnerRepository landOwnerRepository,
                             FactoryOwnerRepository factoryOwnerRepository,
                             LandOwnerFactoryRepository landOwnerFactoryRepository) {
        this.fertilizerRepository = fertilizerRepository;
        this.landOwnerRepository  = landOwnerRepository;
        this.factoryOwnerRepository = factoryOwnerRepository;
        this.landOwnerFactoryRepository = landOwnerFactoryRepository;
    }

    

    public FertilizerResponseDTO requestFertilizer(FertilizerRequestDTO request) {
        LandOwner landOwner = landOwnerRepository.findById(request.getLandOwnerId())
                .or(() -> landOwnerRepository.findByUser_UserId(request.getLandOwnerId()))
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        FactoryOwner factoryOwner = factoryOwnerRepository.findById(request.getFactoryOwnerId())
            .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        LandOwnerFactory registration = landOwnerFactoryRepository
            .findByLandOwnerAndFactory(landOwner, factoryOwner)
            .orElseThrow(() -> new RuntimeException("Land owner is not registered with selected factory"));

        if (registration.getStatus() != LandOwnerFactory.Status.Active) {
            throw new RuntimeException("Selected factory registration is not approved");
        }

        
        BigDecimal cost = request.getQuantity().multiply(UNIT_PRICE);

        FertilizerRequest fertReq = new FertilizerRequest();
        fertReq.setLandOwner(landOwner);
        fertReq.setFactoryOwner(factoryOwner);
        fertReq.setFertilizerType(request.getFertilizerType());
        fertReq.setQuantity(request.getQuantity());
        fertReq.setCost(cost);
        fertReq.setRequestDate(LocalDate.now());
        fertReq.setApprovalStatus(FertilizerRequest.ApprovalStatus.Pending);
        fertilizerRepository.save(fertReq);

        return toDTO(fertReq);
    }

    

    public FertilizerResponseDTO updateStatus(Integer requestId,
                                              FertilizerStatusUpdateDTO update) {
        FertilizerRequest fertReq = fertilizerRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Fertilizer request not found: "
                        + requestId));

        if (fertReq.getApprovalStatus() != FertilizerRequest.ApprovalStatus.Pending) {
            throw new RuntimeException("Request has already been processed");
        }

        if (!java.util.Set.of("Approved", "Rejected").contains(update.getApprovalStatus()))
            throw new IllegalArgumentException("Choose Approved or Rejected");
        fertReq.setApprovalStatus(
                FertilizerRequest.ApprovalStatus.valueOf(update.getApprovalStatus()));
        fertilizerRepository.save(fertReq);

        return toDTO(fertReq);
    }

    

    public List<FertilizerResponseDTO> getRequestsByLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return fertilizerRepository.findByLandOwner(landOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    

    public List<FertilizerResponseDTO> getPendingRequests() {
        return fertilizerRepository
                .findByApprovalStatus(FertilizerRequest.ApprovalStatus.Pending).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

        
        public List<FertilizerResponseDTO> getPendingRequestsByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
            .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return fertilizerRepository
            .findByFactoryOwnerAndApprovalStatus(factoryOwner, FertilizerRequest.ApprovalStatus.Pending)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        }

    

    public List<FertilizerResponseDTO> getAllRequests() {
        return fertilizerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

        
        public List<FertilizerResponseDTO> getAllRequestsByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
            .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return fertilizerRepository.findByFactoryOwner(factoryOwner).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        }

    

    private FertilizerResponseDTO toDTO(FertilizerRequest f) {
        return new FertilizerResponseDTO(
                f.getRequestId(),
                f.getLandOwner().getLandOwnerId(),
                f.getLandOwner().getUser().getName(),
            f.getFactoryOwner() != null ? f.getFactoryOwner().getFactoryOwnerId() : null,
            f.getFactoryOwner() != null && f.getFactoryOwner().getUser() != null
                ? f.getFactoryOwner().getUser().getName() : null,
                f.getFertilizerType(),
                f.getQuantity(),
                f.getCost(),
                f.getApprovalStatus().name(),
                f.getRequestDate()
        );
    }
}