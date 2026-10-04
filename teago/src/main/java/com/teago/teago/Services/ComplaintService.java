package com.teago.teago.Services;

import com.teago.teago.models.Complaint;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.LandOwnerFactory;
import com.teago.teago.Repositories.ComplaintRepository;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.LandOwnerFactoryRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.dto.ComplaintRequestDTO;
import com.teago.teago.dto.ComplaintResponseDTO;
import com.teago.teago.dto.ComplaintResponseUpdateDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@org.springframework.transaction.annotation.Transactional
public class ComplaintService {

    private final ComplaintRepository  complaintRepository;
    private final LandOwnerRepository  landOwnerRepository;
    private final FactoryOwnerRepository factoryOwnerRepository;
    private final LandOwnerFactoryRepository landOwnerFactoryRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            LandOwnerRepository landOwnerRepository,
                            FactoryOwnerRepository factoryOwnerRepository,
                            LandOwnerFactoryRepository landOwnerFactoryRepository) {
        this.complaintRepository = complaintRepository;
        this.landOwnerRepository = landOwnerRepository;
        this.factoryOwnerRepository = factoryOwnerRepository;
        this.landOwnerFactoryRepository = landOwnerFactoryRepository;
    }


    public ComplaintResponseDTO submitComplaint(ComplaintRequestDTO request) {
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

        Complaint complaint = new Complaint();
        complaint.setLandOwner(landOwner);
        complaint.setFactoryOwner(factoryOwner);
        complaint.setComplaintText(request.getComplaintText());
        complaint.setStatus(Complaint.Status.Pending);   // always starts Pending
        complaint.setDateSubmitted(LocalDate.now());
        complaintRepository.save(complaint);

        return toDTO(complaint);
    }

  

    public ComplaintResponseDTO respondToComplaint(Integer complaintId,
                                                    ComplaintResponseUpdateDTO update) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found: "
                        + complaintId));

       
        if(complaint.getStatus()==Complaint.Status.Resolved) throw new IllegalArgumentException("Complaint already resolved");
        if(!"Resolved".equals(update.getStatus())) throw new IllegalArgumentException("Choose Resolved");
        complaint.setResponseText(update.getResponseText());
        complaint.setStatus(Complaint.Status.valueOf(update.getStatus()));
        complaintRepository.save(complaint);

        return toDTO(complaint);
    }

   

    public List<ComplaintResponseDTO> getComplaintsByLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return complaintRepository.findByLandOwner(landOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

  

    public List<ComplaintResponseDTO> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    

    public List<ComplaintResponseDTO> getPendingComplaints() {
        return complaintRepository.findByStatus(Complaint.Status.Pending).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


        public List<ComplaintResponseDTO> getComplaintsByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
            .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return complaintRepository.findByFactoryOwner(factoryOwner).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        }


        public List<ComplaintResponseDTO> getPendingComplaintsByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
            .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return complaintRepository
            .findByFactoryOwnerAndStatus(factoryOwner, Complaint.Status.Pending).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        }


    private ComplaintResponseDTO toDTO(Complaint c) {
        return new ComplaintResponseDTO(
                c.getComplaintId(),
                c.getLandOwner().getLandOwnerId(),
                c.getLandOwner().getUser().getName(),
            c.getFactoryOwner() != null ? c.getFactoryOwner().getFactoryOwnerId() : null,
            c.getFactoryOwner() != null && c.getFactoryOwner().getUser() != null
                ? c.getFactoryOwner().getUser().getName() : null,
                c.getComplaintText(),
                c.getResponseText(),
                c.getStatus().name(),
                c.getDateSubmitted()
        );
    }
}