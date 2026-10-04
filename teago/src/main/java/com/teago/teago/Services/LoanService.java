package com.teago.teago.Services;

import com.teago.teago.models.LandOwner;
import com.teago.teago.models.Loan;
import com.teago.teago.models.FactoryOwner;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.LoanRepository;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.dto.LoanRequestDTO;
import com.teago.teago.dto.LoanResponseDTO;
import com.teago.teago.dto.LoanStatusUpdateDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@org.springframework.transaction.annotation.Transactional
public class LoanService {

    private final com.teago.teago.Repositories.LandOwnerFactoryRepository registrations;
    private final LoanRepository           loanRepository;
    private final LandOwnerRepository      landOwnerRepository;
    private final FactoryOwnerRepository   factoryOwnerRepository;

    public LoanService(LoanRepository loanRepository,
                       LandOwnerRepository landOwnerRepository,
                       FactoryOwnerRepository factoryOwnerRepository, com.teago.teago.Repositories.LandOwnerFactoryRepository registrations) {
        this.registrations = registrations;
        this.loanRepository           = loanRepository;
        this.landOwnerRepository      = landOwnerRepository;
        this.factoryOwnerRepository   = factoryOwnerRepository;
    }


    public LoanResponseDTO applyForLoan(LoanRequestDTO request) {
        LandOwner landOwner = landOwnerRepository.findById(request.getLandOwnerId())
                .orElseThrow(() -> new RuntimeException("Land owner not found"));
        
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(request.getFactoryOwnerId())
                .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        var registration=registrations.findByLandOwnerAndFactory(landOwner,factoryOwner)
            .orElseThrow(() -> new IllegalArgumentException("Not registered with selected factory"));
        if(registration.getStatus()!=com.teago.teago.models.LandOwnerFactory.Status.Active)
            throw new IllegalArgumentException("Factory registration must be active");
        Loan loan = new Loan();
        loan.setLandOwner(landOwner);
        loan.setFactoryOwner(factoryOwner);
        loan.setLoanAmount(request.getLoanAmount());
        loan.setReason(request.getReason());
        loan.setApprovalStatus(Loan.ApprovalStatus.Pending);
        loan.setRequestDate(LocalDate.now());
      
        try {
            String requestType = request.getRequestType() != null ? request.getRequestType() : "LOAN";
            loan.setRequestType(Loan.RequestType.valueOf(requestType));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid request type. Must be LOAN or ADVANCE");
        }
        
        loanRepository.save(loan);

        return toDTO(loan);
    }


    public LoanResponseDTO updateLoanStatus(Integer loanId, LoanStatusUpdateDTO request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        if (loan.getApprovalStatus() != Loan.ApprovalStatus.Pending) {
            throw new RuntimeException("Loan has already been processed");
        }

        Loan.ApprovalStatus newStatus =
                Loan.ApprovalStatus.valueOf(request.getApprovalStatus());
        if(newStatus==Loan.ApprovalStatus.Pending) throw new IllegalArgumentException("Choose Approved or Rejected");
        loan.setApprovalStatus(newStatus);
        
  
        if (newStatus == Loan.ApprovalStatus.Approved) {
            loan.setApprovalDate(LocalDate.now());
        }
        
        loanRepository.save(loan);

        return toDTO(loan);
    }

   

    public List<LoanResponseDTO> getLoansByLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found"));

        return loanRepository.findByLandOwner(landOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

   

    public List<LoanResponseDTO> getLoansByFactoryOwner(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory owner not found"));

        return loanRepository.findByFactoryOwner(factoryOwner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

   

    public List<LoanResponseDTO> getPendingLoans() {
        return loanRepository.findByApprovalStatus(Loan.ApprovalStatus.Pending).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

   

    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

 

    private LoanResponseDTO toDTO(Loan loan) {
        return new LoanResponseDTO(
                loan.getLoanId(),
                loan.getLandOwner().getLandOwnerId(),
                loan.getLandOwner().getUser().getName(),
                loan.getFactoryOwner().getFactoryOwnerId(),
                loan.getFactoryOwner().getUser().getName(),
                loan.getLoanAmount(),
                loan.getReason(),
                loan.getApprovalStatus().name(),
                loan.getRequestDate(),
                loan.getRequestType() != null ? loan.getRequestType().name() : "LOAN"
        );
    }
}