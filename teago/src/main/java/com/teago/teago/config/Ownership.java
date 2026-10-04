package com.teago.teago.config;

import com.teago.teago.Repositories.*;
import com.teago.teago.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;

@Component("ownership")
@RequiredArgsConstructor
public class Ownership {
    private final UserRepository users;
    private final EmployeeRepository employees;
    private final DeliveryRepository deliveries;
    private final LoanRepository loans;
    private final FertilizerRequestRepository fertilizer;
    private final ComplaintRepository complaints;
    private final ChequeRepository cheques;
    private final LandOwnerFactoryRepository registrations;
    public User current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new org.springframework.security.access.AccessDeniedException("Sign in required");
        return users.findByUsername(auth.getName()).orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Sign in required"));
    }
    public boolean owner(Integer id) { var u=current(); return u.getRole()==User.Role.LandOwner && u.getUserId().equals(id); }
    public boolean factory(Integer id) { var u=current(); return u.getRole()==User.Role.FactoryOwner && u.getUserId().equals(id); }
    public boolean employee(Integer id) { return employees.findById(id).map(e -> owner(e.getLandOwner().getLandOwnerId())).orElse(false); }
    public boolean delivery(Integer id) { return deliveries.findById(id).map(d -> factory(d.getFactoryOwner().getFactoryOwnerId())).orElse(false); }
    public boolean readDelivery(Integer id) { return deliveries.findById(id).map(d -> factory(d.getFactoryOwner().getFactoryOwnerId()) || owner(d.getLandOwner().getLandOwnerId())).orElse(false); }
    public boolean loan(Integer id) { return loans.findById(id).map(l -> factory(l.getFactoryOwner().getFactoryOwnerId())).orElse(false); }
    public boolean fertilizer(Integer id) { return fertilizer.findById(id).map(f -> f.getFactoryOwner()!=null && factory(f.getFactoryOwner().getFactoryOwnerId())).orElse(false); }
    public boolean complaint(Integer id) { return complaints.findById(id).map(c -> c.getFactoryOwner()!=null && factory(c.getFactoryOwner().getFactoryOwnerId())).orElse(false); }
    public boolean registration(Integer id) { return registrations.findById(id).map(r -> factory(r.getFactory().getFactoryOwnerId())).orElse(false); }
    public boolean chequeFactory(Integer id) { return cheques.findById(id).map(c -> loan(c.getLoan().getLoanId())).orElse(false); }
    public boolean chequeOwner(Integer id) { return cheques.findById(id).map(c -> owner(c.getLoan().getLandOwner().getLandOwnerId())).orElse(false); }
}
