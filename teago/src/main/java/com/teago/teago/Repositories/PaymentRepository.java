package com.teago.teago.Repositories;

import com.teago.teago.models.Delivery;
import com.teago.teago.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Find the payment linked to a specific delivery
    Optional<Payment> findByDelivery(Delivery delivery);
}