package com.teago.teago.Services;

import com.teago.teago.models.*;
import com.teago.teago.Repositories.*;
import com.teago.teago.dto.DeliveryRequestDTO;
import com.teago.teago.dto.DeliveryResponseDTO;
import com.teago.teago.dto.TeaRateRequestDTO;
import com.teago.teago.dto.TeaRateResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DeliveryService {

    /** Fallback rate used only when a factory has never set a rate. */
    private static final BigDecimal FALLBACK_RATE = BigDecimal.valueOf(50);

    private final DeliveryRepository          deliveryRepository;
    private final FactoryOwnerRepository      factoryOwnerRepository;
    private final LandOwnerRepository         landOwnerRepository;
    private final LandOwnerFactoryRepository  landOwnerFactoryRepository;
    private final PaymentRepository           paymentRepository;
    private final TeaRateRepository           teaRateRepository;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           FactoryOwnerRepository factoryOwnerRepository,
                           LandOwnerRepository landOwnerRepository,
                           LandOwnerFactoryRepository landOwnerFactoryRepository,
                           PaymentRepository paymentRepository,
                           TeaRateRepository teaRateRepository) {
        this.deliveryRepository         = deliveryRepository;
        this.factoryOwnerRepository     = factoryOwnerRepository;
        this.landOwnerRepository        = landOwnerRepository;
        this.landOwnerFactoryRepository = landOwnerFactoryRepository;
        this.paymentRepository          = paymentRepository;
        this.teaRateRepository          = teaRateRepository;
    }


     
    @Transactional
    public DeliveryResponseDTO recordDelivery(DeliveryRequestDTO request) {

        
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(request.getFactoryOwnerId())
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + request.getFactoryOwnerId()));

        
        LandOwner landOwner = landOwnerRepository
                .findById(request.getLandOwnerId())
                .or(() -> landOwnerRepository.findByUser_UserId(request.getLandOwnerId()))
                .orElseThrow(() -> new RuntimeException(
                        "Land owner not found: " + request.getLandOwnerId()));

        
        LandOwnerFactory registration = landOwnerFactoryRepository
                .findByLandOwnerAndFactory(landOwner, factoryOwner)
                .orElseThrow(() -> new RuntimeException(
                        landOwner.getUser().getName()
                        + " is not registered with your factory"));

        if (registration.getStatus() != LandOwnerFactory.Status.Active) {
            throw new RuntimeException(
                    landOwner.getUser().getName()
                    + "'s registration is not yet approved (status: "
                    + registration.getStatus().name() + ")");
        }

        
        BigDecimal rate = resolveRate(request.getRatePerKg(), factoryOwner, request.getDeliveryDate());

        
        Delivery delivery = new Delivery();
        delivery.setFactoryOwner(factoryOwner);
        delivery.setLandOwner(landOwner);
        delivery.setDeliveryDate(request.getDeliveryDate());
        delivery.setTeaWeight(request.getTeaWeight());
        delivery.setDeductionAmount(request.getDeductionAmount());
        delivery.setMonth(request.getDeliveryDate().getMonthValue());
        delivery.setYear(request.getDeliveryDate().getYear());
        deliveryRepository.save(delivery);

        
        BigDecimal total = request.getTeaWeight().multiply(rate);

        Payment payment = new Payment();
        payment.setDelivery(delivery);
        payment.setRatePerKg(rate);
        payment.setTotalAmount(total);
        payment.setPaymentDate(request.getDeliveryDate());
        paymentRepository.save(payment);

        return toDeliveryDTO(delivery, rate, total);
    }

    
    public List<DeliveryResponseDTO> getDeliveriesForFactory(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + factoryOwnerId));

        return deliveryRepository.findByFactoryOwner(factoryOwner)
                .stream()
                .map(this::mapDelivery)
                .collect(Collectors.toList());
    }

    
    public List<DeliveryResponseDTO> getDeliveriesForFactoryAndOwner(Integer factoryOwnerId,
                                                                      Integer landOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + factoryOwnerId));

        LandOwner landOwner = landOwnerRepository
                .findById(landOwnerId)
                .or(() -> landOwnerRepository.findByUser_UserId(landOwnerId))
                .orElseThrow(() -> new RuntimeException(
                        "Land owner not found: " + landOwnerId));

        return deliveryRepository
                .findByFactoryOwnerAndLandOwner(factoryOwner, landOwner)
                .stream()
                .map(this::mapDelivery)
                .collect(Collectors.toList());
    }

    
    public List<DeliveryResponseDTO> getDeliveriesForLandOwner(Integer landOwnerId) {
        LandOwner landOwner = landOwnerRepository
                .findById(landOwnerId)
                .or(() -> landOwnerRepository.findByUser_UserId(landOwnerId))
                .orElseThrow(() -> new RuntimeException(
                        "Land owner not found: " + landOwnerId));

        return deliveryRepository.findByLandOwner(landOwner)
                .stream()
                .map(this::mapDelivery)
                .collect(Collectors.toList());
    }

    
    @Transactional
    public DeliveryResponseDTO updateDeliveryWeight(Integer deliveryId, BigDecimal newTeaWeight) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException(
                        "Delivery not found: " + deliveryId));

        delivery.setTeaWeight(newTeaWeight);
        deliveryRepository.save(delivery);

        
        paymentRepository.findByDelivery(delivery).ifPresent(payment -> {
            BigDecimal newTotal = newTeaWeight.multiply(payment.getRatePerKg());
            payment.setTotalAmount(newTotal);
            paymentRepository.save(payment);
        });

        return mapDelivery(delivery);
    }

    
    @Transactional
    public TeaRateResponseDTO setRate(TeaRateRequestDTO request) {
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(request.getFactoryOwnerId())
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + request.getFactoryOwnerId()));

        TeaRate rate = new TeaRate();
        rate.setFactoryOwner(factoryOwner);
        rate.setRatePerKg(request.getRatePerKg());
        
        rate.setEffectiveFrom(
                request.getEffectiveFrom() != null
                        ? request.getEffectiveFrom()
                        : LocalDate.now());
        teaRateRepository.save(rate);

        return toRateDTO(rate);
    }

    
    public TeaRateResponseDTO getCurrentRate(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + factoryOwnerId));

        return teaRateRepository
                .findFirstByFactoryOwnerAndEffectiveFromLessThanEqualOrderByEffectiveFromDescRateIdDesc(factoryOwner, LocalDate.now())
                .map(this::toRateDTO)
                .orElse(new TeaRateResponseDTO(
                        null,
                        factoryOwnerId,
                        factoryOwner.getFactoryName(),
                        FALLBACK_RATE,
                        LocalDate.now()));
    }

    
    public List<TeaRateResponseDTO> getRateHistory(Integer factoryOwnerId) {
        FactoryOwner factoryOwner = factoryOwnerRepository
                .findById(factoryOwnerId)
                .orElseThrow(() -> new RuntimeException(
                        "Factory owner not found: " + factoryOwnerId));

        return teaRateRepository
                .findByFactoryOwnerOrderByEffectiveFromDesc(factoryOwner)
                .stream()
                .map(this::toRateDTO)
                .collect(Collectors.toList());
    }

    
    private BigDecimal resolveRate(BigDecimal requestedRate, FactoryOwner factoryOwner, LocalDate date) {
        if (requestedRate != null && requestedRate.compareTo(BigDecimal.ZERO) > 0) {
            return requestedRate;
        }
        return teaRateRepository
                .findFirstByFactoryOwnerAndEffectiveFromLessThanEqualOrderByEffectiveFromDescRateIdDesc(factoryOwner, date)
                .map(TeaRate::getRatePerKg)
                .orElse(FALLBACK_RATE);
    }

    
    private DeliveryResponseDTO mapDelivery(Delivery d) {
        BigDecimal rate = paymentRepository.findByDelivery(d)
                .map(Payment::getRatePerKg)
                .orElse(FALLBACK_RATE);
        BigDecimal total = d.getTeaWeight().multiply(rate);
        return toDeliveryDTO(d, rate, total);
    }

    private DeliveryResponseDTO toDeliveryDTO(Delivery d, BigDecimal rate, BigDecimal total) {
        return new DeliveryResponseDTO(
                d.getDeliveryId(),
                d.getFactoryOwner().getFactoryOwnerId(),
                d.getFactoryOwner().getFactoryName(),
                d.getLandOwner().getLandOwnerId(),
                d.getLandOwner().getUser().getName(),
                d.getDeliveryDate(),
                d.getTeaWeight(),
                rate,
                total,
                d.getMonth(),
                d.getYear(),
                d.getDeductionAmount()
        );
    }

    private TeaRateResponseDTO toRateDTO(TeaRate r) {
        return new TeaRateResponseDTO(
                r.getRateId(),
                r.getFactoryOwner().getFactoryOwnerId(),
                r.getFactoryOwner().getFactoryName(),
                r.getRatePerKg(),
                r.getEffectiveFrom()
        );
    }
}