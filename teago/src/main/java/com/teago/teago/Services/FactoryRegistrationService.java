package com.teago.teago.Services;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.LandOwnerFactory;
import com.teago.teago.models.User;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.LandOwnerFactoryRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.UserRepository;
import com.teago.teago.dto.ActiveLandOwnerDTO;
import com.teago.teago.dto.FactoryRegistrationRequestDTO;
import com.teago.teago.dto.FactoryRegistrationResponseDTO;
import com.teago.teago.dto.FactoryRegistrationStatusUpdateDTO;
import com.teago.teago.dto.LandOwnerDetailsDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FactoryRegistrationService demonstrates:
 *
 *  Inheritance    — LandOwnerFactory extends the base JPA entity hierarchy
 *                   and reuses the pattern established by other join models.
 *  Encapsulation  — duplicate registration check, date stamping and initial
 *                   status assignment are hidden from the controller.
 *  Abstraction    — getAllFactories() lets the land owner browse all factories
 *                   without knowing the FactoryOwner entity structure.
 *  Polymorphism   — updateStatus() handles both Active and Rejected transitions
 *                   with one method using the same DTO.
 */
@Service
@org.springframework.transaction.annotation.Transactional
public class FactoryRegistrationService {

    private final LandOwnerFactoryRepository registrationRepository;
    private final LandOwnerRepository        landOwnerRepository;
    private final FactoryOwnerRepository     factoryOwnerRepository;
        private final UserRepository             userRepository;

    public FactoryRegistrationService(LandOwnerFactoryRepository registrationRepository,
                                       LandOwnerRepository landOwnerRepository,
                                                                           FactoryOwnerRepository factoryOwnerRepository,
                                                                           UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.landOwnerRepository    = landOwnerRepository;
        this.factoryOwnerRepository = factoryOwnerRepository;
                this.userRepository         = userRepository;
    }

    // ── Land Owner: register with a factory ──────────────────────────────────

    public FactoryRegistrationResponseDTO register(FactoryRegistrationRequestDTO request) {
        LandOwner landOwner = landOwnerRepository.findById(request.getLandOwnerId())
                .or(() -> landOwnerRepository.findByUser_UserId(request.getLandOwnerId()))
                .orElseGet(() -> createLandOwnerProfile(request.getLandOwnerId()));

        FactoryOwner factory = factoryOwnerRepository.findById(request.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found: "
                        + request.getFactoryId()));

        LandOwnerFactory reg = registrationRepository.findByLandOwnerAndFactory(landOwner, factory).orElseGet(LandOwnerFactory::new);
        if (reg.getRegistrationId() != null && reg.getStatus() != LandOwnerFactory.Status.Rejected)
            throw new IllegalArgumentException("Already registered with this factory");
        reg.setLandOwner(landOwner);
        reg.setFactory(factory);
        reg.setLandSize(request.getLandSize());
        reg.setRegistrationDate(LocalDate.now());
        reg.setStatus(LandOwnerFactory.Status.Pending);  // always starts Pending
        registrationRepository.save(reg);

        return toDTO(reg);
    }

        private LandOwner createLandOwnerProfile(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("Land owner not found"));

                if (user.getRole() != User.Role.LandOwner) {
                        throw new RuntimeException("Selected user is not a land owner");
                }

                LandOwner landOwner = new LandOwner();
                landOwner.setUser(user);
                landOwner.setLandSize(BigDecimal.ZERO);
                return landOwnerRepository.save(landOwner);
        }

    // ── Land Owner: view all their factory registrations ─────────────────────

    public List<FactoryRegistrationResponseDTO> getMyFactories(Integer landOwnerId) {
        return landOwnerRepository.findById(landOwnerId)
                .map(landOwner -> registrationRepository.findByLandOwner(landOwner).stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    // ── Land Owner: browse all available factories to register with ───────────

    public List<FactoryRegistrationResponseDTO> getAllFactories() {
        // Returns all factories as "unregistered" DTOs for browsing
        return factoryOwnerRepository.findAll().stream()
                .map(f -> new FactoryRegistrationResponseDTO(
                        null,
                        null, null, null,
                        f.getFactoryOwnerId(),
                        f.getFactoryName(),
                        f.getFactoryLocation(),
                        f.getUser() != null ? f.getUser().getName() : null,
                        f.getUser() != null ? f.getUser().getContactNumber() : null,
                        null,
                        null,
                        null))
                .collect(Collectors.toList());
    }

    // ── Factory Owner: view all pending registrations for their factory ───────

    public List<FactoryRegistrationResponseDTO> getPendingRegistrations(Integer factoryOwnerId) {
        FactoryOwner factory = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        return registrationRepository
                .findByFactoryAndStatus(factory, LandOwnerFactory.Status.Pending).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Factory Owner: view all registrations under their factory
    public List<FactoryRegistrationResponseDTO> getFactoryRegistrations(Integer factoryOwnerId) {
        FactoryOwner factory = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        return registrationRepository.findByFactory(factory).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Factory Owner: approve or reject a registration ───────────────────────

    public FactoryRegistrationResponseDTO updateStatus(Integer registrationId,
                                                        FactoryRegistrationStatusUpdateDTO req) {
        LandOwnerFactory reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found: "
                        + registrationId));

        if(reg.getStatus()!=LandOwnerFactory.Status.Pending) throw new IllegalArgumentException("Registration already processed");
        if(!java.util.Set.of("Active","Rejected").contains(req.getStatus())) throw new IllegalArgumentException("Choose Active or Rejected");
        reg.setStatus(LandOwnerFactory.Status.valueOf(req.getStatus()));
        registrationRepository.save(reg);
        return toDTO(reg);
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private FactoryRegistrationResponseDTO toDTO(LandOwnerFactory r) {
        return new FactoryRegistrationResponseDTO(
                r.getRegistrationId(),
                r.getLandOwner().getLandOwnerId(),
                r.getLandOwner().getUser().getName(),
                r.getLandOwner().getUser().getNic(),
                r.getFactory().getFactoryOwnerId(),
                r.getFactory().getFactoryName(),
                r.getFactory().getFactoryLocation(),
                                r.getFactory().getUser() != null ? r.getFactory().getUser().getName() : null,
                                r.getFactory().getUser() != null ? r.getFactory().getUser().getContactNumber() : null,
                r.getLandSize(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getRegistrationDate()
        );
    }

    
    public List<ActiveLandOwnerDTO> getActiveLandOwners(Integer factoryOwnerId) {
        FactoryOwner factory = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory not found: " + factoryOwnerId));

        return registrationRepository
                .findByFactoryAndStatus(factory, LandOwnerFactory.Status.Active)
                .stream()
                .map(reg -> new ActiveLandOwnerDTO(
                        reg.getLandOwner().getLandOwnerId(),
                        reg.getLandOwner().getUser().getName()))
                .sorted(Comparator.comparing(ActiveLandOwnerDTO::getName))
                .collect(Collectors.toList());
    }

    
    public LandOwnerDetailsDTO getLandOwnerDetails(Integer factoryOwnerId, Integer landOwnerId) {
        FactoryOwner factory = factoryOwnerRepository.findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException("Factory not found: " + factoryOwnerId));

        LandOwner landOwner = landOwnerRepository.findById(landOwnerId)
                .orElseThrow(() -> new RuntimeException("Land owner not found: " + landOwnerId));

        
        LandOwnerFactory registration = registrationRepository.findByLandOwnerAndFactory(landOwner, factory)
                .orElseThrow(() -> new RuntimeException("Land owner not registered with this factory"));

        User user = landOwner.getUser();
        LandOwnerDetailsDTO dto = new LandOwnerDetailsDTO();

        
        dto.setLandOwnerId(landOwner.getLandOwnerId());
        dto.setName(user.getName());
        dto.setNic(user.getNic());
        dto.setAddress(user.getAddress());
        dto.setContactNumber(user.getContactNumber());
        dto.setNameWithInitials(landOwner.getNameWithInitials());
        dto.setFullName(landOwner.getFullName());
        dto.setDateOfBirth(landOwner.getDateOfBirth());

        
        dto.setTotalLandSize(landOwner.getLandSize());
        dto.setLandLocation(landOwner.getLandLocation());
        dto.setFactoryLandSize(registration.getLandSize());

        
        dto.setBankAccountName(landOwner.getBankAccountName());
        dto.setBankName(landOwner.getBankName());
        dto.setBankBranch(landOwner.getBankBranch());
        dto.setBankAccountNumber(landOwner.getBankAccountNumber());

        
        dto.setEmergencyContactName(landOwner.getEmergencyContactName());
        dto.setEmergencyContactAddress(landOwner.getEmergencyContactAddress());
        dto.setEmergencyContactPhone(landOwner.getEmergencyContactPhone());
        dto.setEmergencyContactRelationship(landOwner.getEmergencyContactRelationship());

        
        dto.setRegistrationId(registration.getRegistrationId());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus().name());

        return dto;
    }

    
}