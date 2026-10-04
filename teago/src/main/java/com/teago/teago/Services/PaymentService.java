package com.teago.teago.Services;
import com.teago.teago.Repositories.*;
import com.teago.teago.models.*;
import com.teago.teago.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class PaymentService {
    private final PaymentRepository payments;
    private final DeliveryRepository deliveries;
    private final LandOwnerRepository owners;
    private final FactoryOwnerRepository factories;
    private final LandOwnerFactoryRepository registrations;
    private final MonthlyBillService bills;
    private final SettlementRepository settlements;

    @Transactional
    public PaymentDTO createPayment(PaymentRequest request) {
        Delivery delivery=deliveries.findById(request.getDeliveryId()).orElseThrow(() -> new IllegalArgumentException("Delivery not found"));
        owners.lockById(delivery.getLandOwner().getLandOwnerId()).orElseThrow();
        var existing=payments.findByDelivery(delivery);
        if(existing.isPresent()) {
            if(existing.get().getRatePerKg().compareTo(request.getRatePerKg())!=0 || !existing.get().getPaymentDate().equals(request.getPaymentDate()))
                throw new IllegalArgumentException("Payment already exists; the recorded rate cannot be replaced");
            return dto(existing.get());
        }
        Payment p=new Payment(); p.setDelivery(delivery); p.setRatePerKg(request.getRatePerKg());
        p.setTotalAmount(delivery.getTeaWeight().multiply(request.getRatePerKg()).setScale(2,RoundingMode.HALF_UP));
        p.setPaymentDate(request.getPaymentDate()); return dto(payments.save(p));
    }
    public List<PaymentDTO> getAllPayments() { return payments.findAll().stream().map(this::dto).toList(); }
    public PaymentDTO getPaymentByDelivery(Integer id) {
        return dto(payments.findByDelivery(deliveries.findById(id).orElseThrow(() -> new IllegalArgumentException("Delivery not found")))
            .orElseThrow(() -> new IllegalArgumentException("Payment not found")));
    }
    public List<PaymentDTO> getPaymentsByLandOwner(Integer id) {
        var owner=owners.findById(id).orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        return deliveries.findByLandOwner(owner).stream().map(payments::findByDelivery).flatMap(Optional::stream).map(this::dto).toList();
    }
    public List<PaymentSummaryDTO> getPaymentSummariesByFactory(Integer id) {
        var factory=factories.findById(id).orElseThrow(() -> new IllegalArgumentException("Factory not found"));
        YearMonth now=YearMonth.now();
        return registrations.findByFactoryAndStatus(factory,LandOwnerFactory.Status.Active).stream().map(r -> {
            var b=bills.preview(r.getLandOwner().getLandOwnerId(),id,now.getMonthValue(),now.getYear());
            var settlement=settlements.findByFactory_FactoryOwnerIdAndOwner_LandOwnerIdAndMonthAndYear(id,r.getLandOwner().getLandOwnerId(),now.getMonthValue(),now.getYear());
            String status=settlement.map(s -> s.getAmount().compareTo(b.getNetPayable())==0?"Processed":"Review required").orElse("Pending");
            return new PaymentSummaryDTO(b.getLandOwnerId(),b.getLandOwnerName(),b.getTotalEarnings(),b.getTotalDeductions(),b.getNetPayable(),status);
        }).toList();
    }
    public FactoryPaymentSummaryDTO getFactoryPaymentSummary(Integer id) {
        var rows=getPaymentSummariesByFactory(id);
        return new FactoryPaymentSummaryDTO(rows.stream().map(PaymentSummaryDTO::getGrossAmount).reduce(BigDecimal.ZERO,BigDecimal::add),
            rows.stream().map(PaymentSummaryDTO::getDeductionsAmount).reduce(BigDecimal.ZERO,BigDecimal::add),
            rows.stream().map(PaymentSummaryDTO::getNetAmount).reduce(BigDecimal.ZERO,BigDecimal::add),
            (int)rows.stream().filter(r -> r.getStatus().equals("Processed")).count(), rows.size());
    }
    @Transactional
    public Map<String,Object> recordSettlement(Integer factoryId,SettlementRequest request) {
        var owner=owners.lockById(request.landOwnerId()).orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        var existing=settlements.findByFactory_FactoryOwnerIdAndOwner_LandOwnerIdAndMonthAndYear(factoryId,request.landOwnerId(),request.month(),request.year());
        if(existing.isPresent()) {
            if(!existing.get().getMethod().equals(request.method()) || !existing.get().getReference().equals(request.reference()))
                throw new IllegalArgumentException("A settlement is already recorded for this month");
            return Map.of("id",existing.get().getId(),"message","Settlement already recorded");
        }
        var bill=bills.generateBillForFactoryOwner(factoryId,request.landOwnerId(),request.month(),request.year());
        if(bill.getNetPayable().signum()<=0) throw new IllegalArgumentException("No positive amount is payable");
        Settlement s=new Settlement(); s.setFactory(factories.findById(factoryId).orElseThrow()); s.setOwner(owner);
        s.setMonth(request.month()); s.setYear(request.year()); s.setAmount(bill.getNetPayable()); s.setMethod(request.method());
        s.setReference(request.reference()); s.setRecordedDate(LocalDate.now()); settlements.save(s);
        return Map.of("id",s.getId(),"message","External payment recorded");
    }
    private PaymentDTO dto(Payment p) {
        var d=p.getDelivery(); BigDecimal deduction=d.getDeductionAmount()==null?BigDecimal.ZERO:d.getDeductionAmount();
        return new PaymentDTO(p.getPaymentId(),d.getDeliveryId(),d.getDeliveryDate(),d.getTeaWeight(),d.getLandOwner().getLandOwnerId(),
            d.getLandOwner().getUser().getName(),p.getRatePerKg(),p.getTotalAmount(),p.getPaymentDate(),BigDecimal.ZERO,BigDecimal.ZERO,deduction,p.getTotalAmount().subtract(deduction));
    }
}
