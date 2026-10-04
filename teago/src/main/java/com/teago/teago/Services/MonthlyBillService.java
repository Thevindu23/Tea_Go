package com.teago.teago.Services;

import com.teago.teago.models.*;
import com.teago.teago.Repositories.*;
import com.teago.teago.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class MonthlyBillService {
    private final MonthlyBillRepository bills;
    private final LandOwnerRepository owners;
    private final FactoryOwnerRepository factories;
    private final LandOwnerFactoryRepository registrations;
    private final DeliveryRepository deliveries;
    private final PaymentRepository payments;
    private final LoanRepository loans;
    private final FertilizerRequestRepository fertilizers;

    @Transactional
    public MonthlyBillResponseDTO generateBill(Integer ownerId, Integer month, Integer year) {
        return save(ownerId, 0, month, year);
    }
    @Transactional
    public MonthlyBillResponseDTO generateBillForFactoryOwner(Integer factoryId, Integer ownerId, Integer month, Integer year) {
        requireRegistration(factoryId, ownerId);
        return save(ownerId, factoryId, month, year);
    }
    @Transactional
    public List<MonthlyBillResponseDTO> generateBillsForFactoryOwner(Integer factoryId, Integer month, Integer year) {
        period(month, year);
        return registrations.findByFactoryAndStatus(factory(factoryId), LandOwnerFactory.Status.Active).stream()
            .map(r -> save(r.getLandOwner().getLandOwnerId(), factoryId, month, year)).toList();
    }
    public List<MonthlyBillResponseDTO> getBillsByLandOwner(Integer ownerId) {
        return sorted(bills.findByLandOwnerAndScopeId(owner(ownerId), 0));
    }
    public List<MonthlyBillResponseDTO> getBillsByFactoryOwner(Integer factoryId) {
        factory(factoryId);
        return sorted(bills.findByScopeId(factoryId));
    }
    public IncomeStatementResponseDTO generateIncomeStatement(Integer ownerId, Integer months, Integer endMonth, Integer endYear) {
        return statement(ownerId, 0, months, endMonth, endYear);
    }
    public IncomeStatementResponseDTO generateIncomeStatementForFactoryOwner(Integer factoryId, Integer ownerId, Integer months, Integer endMonth, Integer endYear) {
        requireRegistration(factoryId, ownerId);
        return statement(ownerId, factoryId, months, endMonth, endYear);
    }
    // Pure calculation: viewing a statement never creates or changes a bill.
    public MonthlyBillResponseDTO preview(Integer ownerId, Integer scopeId, Integer month, Integer year) {
        return dto(calculate(owner(ownerId), scopeId, period(month, year)));
    }
    private MonthlyBillResponseDTO save(Integer ownerId, Integer scope, Integer month, Integer year) {
        YearMonth target=period(month,year);
        LandOwner owner=owners.lockById(ownerId).orElseThrow(() -> new IllegalArgumentException("Land owner not found"));
        MonthlyBill calculated=calculate(owner,scope,target);
        bills.findByLandOwnerAndScopeIdAndMonthAndYear(owner,scope,month,year).ifPresent(b -> calculated.setBillId(b.getBillId()));
        return dto(bills.save(calculated));
    }
    private MonthlyBill calculate(LandOwner owner, Integer scope, YearMonth target) {
        BigDecimal earnings=BigDecimal.ZERO, deliveryDed=BigDecimal.ZERO, loanDed=BigDecimal.ZERO, fertilizerDed=BigDecimal.ZERO;
        for (Delivery d : deliveries.findByLandOwnerAndMonthAndYear(owner,target.getMonthValue(),target.getYear())) {
            if (scope!=0 && !scope.equals(d.getFactoryOwner().getFactoryOwnerId())) continue;
            earnings=earnings.add(payments.findByDelivery(d).map(Payment::getTotalAmount).orElse(BigDecimal.ZERO));
            deliveryDed=deliveryDed.add(zero(d.getDeductionAmount()));
        }
        for (Loan l : loans.findByLandOwnerAndApprovalStatus(owner,Loan.ApprovalStatus.Approved)) {
            if (l.getApprovalDate()==null || (scope!=0 && !scope.equals(l.getFactoryOwner().getFactoryOwnerId()))) continue;
            YearMonth approval=YearMonth.from(l.getApprovalDate());
            // Preserve the original project's 10% approval-month loan deduction.
            // Advances are deducted once, in the following month, independent of report generation order.
            if (l.getRequestType()==Loan.RequestType.ADVANCE) {
                if (target.equals(approval.plusMonths(1))) loanDed=loanDed.add(l.getLoanAmount());
            } else if (target.equals(approval)) loanDed=loanDed.add(l.getLoanAmount().multiply(new BigDecimal("0.10")));
        }
        for (FertilizerRequest f : fertilizers.findByLandOwner(owner)) {
            if (f.getApprovalStatus()!=FertilizerRequest.ApprovalStatus.Approved || f.getRequestDate()==null) continue;
            if (scope!=0 && (f.getFactoryOwner()==null || !scope.equals(f.getFactoryOwner().getFactoryOwnerId()))) continue;
            if (target.equals(YearMonth.from(f.getRequestDate()))) fertilizerDed=fertilizerDed.add(f.getCost());
        }
        MonthlyBill b=new MonthlyBill();
        b.setLandOwner(owner); b.setScopeId(scope); b.setMonth(target.getMonthValue()); b.setYear(target.getYear());
        b.setTotalEarnings(money(earnings)); b.setDeliveryDeductions(money(deliveryDed));
        b.setLoanDeductions(money(loanDed)); b.setFertilizerDeductions(money(fertilizerDed));
        b.setTotalDeductions(b.getDeliveryDeductions().add(b.getLoanDeductions()).add(b.getFertilizerDeductions()));
        // Preserve a negative balance instead of silently losing excess deductions.
        b.setNetPayable(b.getTotalEarnings().subtract(b.getTotalDeductions()));
        b.setGeneratedDate(LocalDate.now()); return b;
    }
    private IncomeStatementResponseDTO statement(Integer ownerId,Integer scope,Integer months,Integer endMonth,Integer endYear) {
        if(months==null || months<1 || months>24) throw new IllegalArgumentException("Period must be 1 to 24 months");
        YearMonth end=period(endMonth,endYear); LandOwner owner=owner(ownerId);
        List<IncomeStatementMonthDTO> rows=new ArrayList<>();
        BigDecimal earnings=BigDecimal.ZERO,deductions=BigDecimal.ZERO,net=BigDecimal.ZERO;
        for(int i=months-1;i>=0;i--) {
            MonthlyBill b=calculate(owner,scope,end.minusMonths(i));
            rows.add(new IncomeStatementMonthDTO(b.getMonth(),b.getYear(),b.getTotalEarnings(),b.getTotalDeductions(),b.getNetPayable()));
            earnings=earnings.add(b.getTotalEarnings()); deductions=deductions.add(b.getTotalDeductions()); net=net.add(b.getNetPayable());
        }
        return new IncomeStatementResponseDTO(ownerId,owner.getUser().getName(),months,endMonth,endYear,rows,earnings,deductions,net,LocalDate.now());
    }
    private List<MonthlyBillResponseDTO> sorted(List<MonthlyBill> rows) {
        return rows.stream().sorted(Comparator.comparing(MonthlyBill::getYear).thenComparing(MonthlyBill::getMonth).reversed()).map(this::dto).toList();
    }
    private MonthlyBillResponseDTO dto(MonthlyBill b) {
        return new MonthlyBillResponseDTO(b.getBillId(),b.getLandOwner().getLandOwnerId(),b.getLandOwner().getUser().getName(),b.getMonth(),b.getYear(),
            b.getTotalEarnings(),b.getDeliveryDeductions(),zero(b.getLoanDeductions()),zero(b.getFertilizerDeductions()),b.getTotalDeductions(),b.getNetPayable(),b.getGeneratedDate());
    }
    private LandOwner owner(Integer id) { return owners.findById(id).orElseThrow(() -> new IllegalArgumentException("Land owner not found")); }
    private FactoryOwner factory(Integer id) { return factories.findById(id).orElseThrow(() -> new IllegalArgumentException("Factory not found")); }
    private void requireRegistration(Integer factoryId,Integer ownerId) {
        var reg=registrations.findByLandOwnerAndFactory(owner(ownerId),factory(factoryId)).orElseThrow(() -> new IllegalArgumentException("No factory registration"));
        if(reg.getStatus()!=LandOwnerFactory.Status.Active) throw new IllegalArgumentException("Factory registration must be active");
    }
    private YearMonth period(Integer month,Integer year) {
        if(month==null || month<1 || month>12 || year==null || year<2000 || year>3000) throw new IllegalArgumentException("Invalid month or year");
        return YearMonth.of(year,month);
    }
    private BigDecimal zero(BigDecimal n) { return n==null?BigDecimal.ZERO:n; }
    private BigDecimal money(BigDecimal n) { return n.setScale(2,RoundingMode.HALF_UP); }
}
