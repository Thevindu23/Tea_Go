package com.teago.teago.Services;

import com.teago.teago.models.Cheque;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.Loan;
import com.teago.teago.Repositories.ChequeRepository;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.LoanRepository;

import com.teago.teago.dto.ChequeIssueRequestDTO;
import com.teago.teago.dto.ChequeResponseDTO;
import com.teago.teago.dto.ChequeStatusUpdateDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@org.springframework.transaction.annotation.Transactional
public class ChequeService {

    private final ChequeRepository     chequeRepository;
    private final LoanRepository       loanRepository;
    private final LandOwnerRepository  landOwnerRepository;
        private final FactoryOwnerRepository factoryOwnerRepository;

    public ChequeService(ChequeRepository chequeRepository,
                         LoanRepository loanRepository,
                                                 
                                                 LandOwnerRepository landOwnerRepository,
                                                 FactoryOwnerRepository factoryOwnerRepository) {
        this.chequeRepository     = chequeRepository;
        this.loanRepository       = loanRepository;
        this.landOwnerRepository = landOwnerRepository;
                this.factoryOwnerRepository = factoryOwnerRepository;
    }


    public ChequeResponseDTO issueCheque(ChequeIssueRequestDTO request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found: "
                        + request.getLoanId()));

        if (loan.getApprovalStatus() != Loan.ApprovalStatus.Approved) {
            throw new RuntimeException("Cannot issue cheque for a non-approved loan");
        }

        var existing=chequeRepository.findByLoan(loan);
        if(existing.isPresent()) return toDTO(existing.get());
        Cheque cheque = new Cheque();
        cheque.setLoan(loan);
        cheque.setIssueDate(LocalDate.now());
        cheque.setDeliveryStatus(Cheque.DeliveryStatus.Pending);
        cheque.setVerificationStatus(Cheque.VerificationStatus.Pending);
        chequeRepository.save(cheque);

        return toDTO(cheque);
    }



    public ChequeResponseDTO updateDeliveryStatus(Integer chequeId,
                                                   ChequeStatusUpdateDTO update) {
        Cheque cheque = findCheque(chequeId);
        var next=Cheque.DeliveryStatus.valueOf(update.getStatus());
        var previous=cheque.getDeliveryStatus();
        if(next==previous) return toDTO(cheque);
        boolean valid=(previous==Cheque.DeliveryStatus.Pending && next==Cheque.DeliveryStatus.In_Transit)
            || (previous==Cheque.DeliveryStatus.In_Transit && (next==Cheque.DeliveryStatus.Delivered || next==Cheque.DeliveryStatus.Returned))
            || (previous==Cheque.DeliveryStatus.Returned && next==Cheque.DeliveryStatus.In_Transit);
        if(!valid) throw new IllegalArgumentException("Invalid cheque delivery transition");
        cheque.setDeliveryStatus(next);
        chequeRepository.save(cheque);
        return toDTO(cheque);
    }


    public ChequeResponseDTO markChequeAsIssued(Integer chequeId) {
        Cheque cheque = findCheque(chequeId);
        if (cheque.getIssueDate() != null) {
            return toDTO(cheque);
        }
        cheque.setIssueDate(LocalDate.now());
        chequeRepository.save(cheque);
        return toDTO(cheque);
    }

  

    public ChequeResponseDTO updateVerificationStatus(Integer chequeId,
                                                       ChequeStatusUpdateDTO update) {
        Cheque cheque = findCheque(chequeId);
        if(cheque.getDeliveryStatus()!=Cheque.DeliveryStatus.Delivered) throw new IllegalArgumentException("Cheque has not been delivered");
        var next=Cheque.VerificationStatus.valueOf(update.getStatus());
        if(next==cheque.getVerificationStatus()) return toDTO(cheque);
        if(cheque.getVerificationStatus()!=Cheque.VerificationStatus.Pending || next==Cheque.VerificationStatus.Pending)
            throw new IllegalArgumentException("Invalid verification transition");
        cheque.setVerificationStatus(next);
        chequeRepository.save(cheque);
        return toDTO(cheque);
    }

 

    public List<ChequeResponseDTO> getAllCheques() {
        return chequeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<ChequeResponseDTO> getChequesBySupervisor(Integer supervisorId) {
        // Cheque entity no longer stores supervisor linkage directly.
        // Keep endpoint stable by returning all cheques for now.
        return chequeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<ChequeResponseDTO> getChequesByLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return chequeRepository.findByLoan_LandOwner(landOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<ChequeResponseDTO> getChequesByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return chequeRepository.findByLoan_FactoryOwner(factoryOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    private Cheque findCheque(Integer chequeId) {
        return chequeRepository.findById(chequeId)
                .orElseThrow(() -> new RuntimeException("Cheque not found: " + chequeId));
    }

    private ChequeResponseDTO toDTO(Cheque c) {
        return new ChequeResponseDTO(
                c.getChequeId(),
                c.getLoan().getLoanId(),
                c.getLoan().getLoanAmount(),
                c.getLoan().getLandOwner().getLandOwnerId(),
                c.getLoan().getLandOwner().getUser().getName(),
                "Factory owner",
                c.getIssueDate(),
                c.getDeliveryStatus().name(),
                c.getVerificationStatus().name()
        );
    }
}