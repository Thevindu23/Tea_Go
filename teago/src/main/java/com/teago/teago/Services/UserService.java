package com.teago.teago.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.models.LandOwner;
import com.teago.teago.models.User;
import com.teago.teago.Repositories.FactoryOwnerRepository;
import com.teago.teago.Repositories.LandOwnerRepository;
import com.teago.teago.Repositories.UserRepository;
import com.teago.teago.dto.LoginRequest;
import com.teago.teago.dto.RegisterRequest;
import com.teago.teago.dto.UserDTO;

import java.time.LocalDate;

@Service
public class UserService {

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final UserRepository         userRepository;
    private final LandOwnerRepository    landOwnerRepository;
    private final FactoryOwnerRepository factoryOwnerRepository;

    public UserService(UserRepository userRepository,
                       LandOwnerRepository landOwnerRepository,
                       FactoryOwnerRepository factoryOwnerRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository         = userRepository;
        this.landOwnerRepository    = landOwnerRepository;
        this.factoryOwnerRepository = factoryOwnerRepository;
    }

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already exists");

        if (userRepository.existsByNic(request.getNic()))
            throw new RuntimeException("NIC already registered");

        User.Role role = mapRole(request.getRole());
        if (role == User.Role.Supervisor) throw new IllegalArgumentException("Supervisor registration is not available");

        
        User user = new User();
        user.setName(request.getName());
        user.setNic(request.getNic());
        user.setAddress(request.getAddress());
        user.setContactNumber(request.getContactNumber());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);

       
        switch (role) {
            case LandOwner:    createLandOwnerRow(user, request);    break;
            case FactoryOwner: createFactoryOwnerRow(user, request); break;
            default: throw new RuntimeException("Invalid role: " + role);
        }
    }


    private void createLandOwnerRow(User user, RegisterRequest req) {
        LandOwner lo = new LandOwner();
        lo.setUser(user);

       
        lo.setNameWithInitials(req.getNameWithInitials());
        lo.setFullName(req.getFullName());

        if (req.getDateOfBirth() != null && !req.getDateOfBirth().isBlank()) {
            try { lo.setDateOfBirth(LocalDate.parse(req.getDateOfBirth())); }
            catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("Invalid date of birth"); }
        }

      
        lo.setLandSize(req.getLandSize());
        lo.setLandLocation(req.getLandLocation());

       
        lo.setBankAccountName(req.getBankAccountName());
        lo.setBankName(req.getBankName());
        lo.setBankBranch(req.getBankBranch());
        lo.setBankAccountNumber(req.getBankAccountNumber());

        
        lo.setEmergencyContactName(req.getEmergencyContactName());
        lo.setEmergencyContactAddress(req.getEmergencyContactAddress());
        lo.setEmergencyContactPhone(req.getEmergencyContactPhone());
        lo.setEmergencyContactRelationship(req.getEmergencyContactRelationship());

        landOwnerRepository.save(lo);
    }

    private void createFactoryOwnerRow(User user, RegisterRequest req) {
        FactoryOwner fo = new FactoryOwner();
        fo.setUser(user);
       
        fo.setFullName(
            req.getFactoryOwnerFullName() != null && !req.getFactoryOwnerFullName().isBlank()
                ? req.getFactoryOwnerFullName() : user.getName()
        );
        fo.setFactoryName(
            req.getFactoryName() != null && !req.getFactoryName().isBlank()
                ? req.getFactoryName() : user.getName() + "'s Factory"
        );
        fo.setFactoryLocation(
            req.getFactoryLocation() != null ? req.getFactoryLocation() : ""
        );
        fo.setRegistrationNumber(req.getRegistrationNumber());
        factoryOwnerRepository.save(fo);
    }

    private User.Role mapRole(String roleStr) {
        if (roleStr == null) throw new RuntimeException("Role is required");
        switch (roleStr.toLowerCase().trim()) {
            case "land_owner":    return User.Role.LandOwner;
            case "factory_owner": return User.Role.FactoryOwner;
            default:
                try   { return User.Role.valueOf(roleStr); }
                catch (Exception e) { throw new RuntimeException("Invalid role: " + roleStr); }
        }
    }



    public UserDTO login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password");

        if (user.getRole() == User.Role.Supervisor)
            throw new RuntimeException("Supervisor access is not available");
        return new UserDTO(
                user.getName(), user.getNic(), user.getAddress(),
                user.getContactNumber(), user.getUsername(),
                null, user.getRole().toString()
        );
    }
}