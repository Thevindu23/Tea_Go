package com.teago.teago;

import com.teago.teago.Repositories.*;
import com.teago.teago.Services.*;
import com.teago.teago.models.*;
import com.teago.teago.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class WorkflowTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired LandOwnerRepository owners;
    @Autowired FactoryOwnerRepository factories;
    @Autowired LandOwnerFactoryRepository registrations;
    @Autowired EmployeeRepository employees;
    @Autowired PaymentRepository payments;
    @Autowired LoanRepository loans;
    @Autowired MonthlyBillRepository billRepository;
    @Autowired ChequeRepository cheques;
    @Autowired DailyWageRepository wages;
    @Autowired TeaCollectionRepository collections;
    @Autowired PasswordEncoder encoder;
    @Autowired DeliveryService deliveries;
    @Autowired MonthlyBillService bills;
    @Autowired UserService userService;

    User user(String name,User.Role role) {
        User u=new User(); u.setUsername(name); u.setName(name); u.setNic(name); u.setRole(role);
        u.setPassword(encoder.encode("TestPass123!")); return users.save(u);
    }
    LandOwner owner(String name) {
        LandOwner o=new LandOwner(); o.setUser(user(name,User.Role.LandOwner)); return owners.save(o);
    }
    FactoryOwner factory(String name) {
        FactoryOwner f=new FactoryOwner(); f.setUser(user(name,User.Role.FactoryOwner)); f.setFactoryName(name); return factories.save(f);
    }
    void connect(LandOwner o,FactoryOwner f) {
        LandOwnerFactory r=new LandOwnerFactory(); r.setLandOwner(o); r.setFactory(f);
        r.setStatus(LandOwnerFactory.Status.Active); r.setRegistrationDate(LocalDate.now()); registrations.save(r);
    }
    MockHttpSession login(String username) throws Exception {
        MvcResult result=mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
            .content("{\"username\":\""+username+"\",\"password\":\"TestPass123!\"}"))
            .andExpect(status().isOk()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
    DeliveryResponseDTO delivery(LandOwner o,FactoryOwner f,LocalDate date,String rate) {
        DeliveryRequestDTO r=new DeliveryRequestDTO(); r.setLandOwnerId(o.getLandOwnerId()); r.setFactoryOwnerId(f.getFactoryOwnerId());
        r.setDeliveryDate(date); r.setTeaWeight(new BigDecimal("10")); r.setRatePerKg(rate==null?null:new BigDecimal(rate));
        r.setDeductionAmount(new BigDecimal("5")); return deliveries.recordDelivery(r);
    }
    Employee employee(LandOwner o) {
        Employee e=new Employee(); e.setLandOwner(o); e.setEmployeeName("Worker"); e.setWorkDays(1); e.setSalaryRate(new BigDecimal("100")); return employees.save(e);
    }

    @Test void authenticationHashingCsrfAndLogout() throws Exception {
        mvc.perform(get("/api/payments")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
            .content("{\"name\":\"Owner\",\"nic\":\"123456789V\",\"username\":\"owner\",\"password\":\"TestPass123!\",\"role\":\"land_owner\"}"))
            .andExpect(status().isOk());
        User u=users.findByUsername("owner").orElseThrow();
        assertNotEquals("TestPass123!",u.getPassword()); assertTrue(encoder.matches("TestPass123!",u.getPassword()));
        MockHttpSession session=login("owner");
        mvc.perform(get("/api/auth/session").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("LAND_OWNER"));
        mvc.perform(get("/api/dashboard/land-owner").session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/dashboard/factory-owner").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/payments").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isOk());
        assertTrue(session.isInvalid());
    }

    @Test void ownersCannotReadOrChangeOtherOwnersRecords() throws Exception {
        var a=owner("ownerA"); var b=owner("ownerB"); var e=employee(b); var session=login("ownerA");
        mvc.perform(get("/api/employees/owner/"+b.getLandOwnerId()).session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/employees/owner/"+a.getLandOwnerId()).session(session)).andExpect(status().isOk());
        mvc.perform(post("/api/daily-wages").session(session).with(csrf()).contentType("application/json")
            .content("{\"employeeId\":"+e.getEmployeeId()+",\"wageDate\":\"2026-01-01\",\"dailyRate\":100}"))
            .andExpect(status().isForbidden());
    }

    @Test void factoryOwnershipChecksAndMissingCsrfAreEnforced() throws Exception {
        var a=factory("factoryA"); var b=factory("factoryB"); var session=login("factoryA");
        mvc.perform(get("/api/payments/factory/"+b.getFactoryOwnerId()).session(session)).andExpect(status().isForbidden());
        String body="{\"factoryOwnerId\":"+a.getFactoryOwnerId()+",\"ratePerKg\":60}";
        mvc.perform(post("/api/deliveries/rate").session(session).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/deliveries/rate").session(session).with(csrf()).contentType("application/json").content(body)).andExpect(status().isCreated());
    }

    @Test void dailyRecordsUpsertValidateAndKeepOwnersIsolated() throws Exception {
        var a=owner("ownerA"); var b=owner("ownerB"); var e=employee(a); var session=login("ownerA");
        String wage="{\"employeeId\":"+e.getEmployeeId()+",\"wageDate\":\"2026-01-01\",\"dailyRate\":100}";
        for(int i=0;i<2;i++) mvc.perform(post("/api/daily-wages").session(session).with(csrf()).contentType("application/json").content(wage)).andExpect(status().isOk());
        assertEquals(1,wages.count());
        mvc.perform(post("/api/daily-wages").session(session).with(csrf()).contentType("application/json").content(wage.replace(":100",":-1"))).andExpect(status().isBadRequest());
        String collection="{\"employeeId\":"+e.getEmployeeId()+",\"collectionDate\":\"2026-01-01\",\"quantityKg\":12.5,\"quality\":\"Green\"}";
        for(int i=0;i<2;i++) mvc.perform(post("/api/tea-collections").session(session).with(csrf()).contentType("application/json").content(collection)).andExpect(status().isOk());
        assertEquals(1,collections.count());
        mvc.perform(get("/api/tea-collections/owner/"+a.getLandOwnerId()+"/date-range?startDate=2026-01-01&endDate=2026-01-31").session(session))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].quantityKg").value(12.5));
        Integer collectionId=collections.findAll().getFirst().getCollectionId();
        mvc.perform(delete("/api/tea-collections/"+collectionId).session(login("ownerB")).with(csrf())).andExpect(status().isForbidden());
    }

    @Test void billsAreFactoryScopedRepeatableAndStatementsAreReadOnly() {
        var o=owner("ownerA"); var f=factory("factoryA"); var other=factory("factoryB"); connect(o,f); connect(o,other);
        delivery(o,f,LocalDate.of(2026,1,10),"50"); delivery(o,other,LocalDate.of(2026,1,10),"80");
        var first=bills.generateBillForFactoryOwner(f.getFactoryOwnerId(),o.getLandOwnerId(),1,2026);
        assertEquals(0,new BigDecimal("500").compareTo(first.getTotalEarnings()));
        var again=bills.generateBillForFactoryOwner(f.getFactoryOwnerId(),o.getLandOwnerId(),1,2026);
        assertEquals(first.getBillId(),again.getBillId()); assertEquals(1,billRepository.count());
        assertTrue(bills.getBillsByFactoryOwner(other.getFactoryOwnerId()).isEmpty());
        var statement=bills.generateIncomeStatement(o.getLandOwnerId(),3,2,2026);
        assertEquals(1,billRepository.count());
        var total=bills.preview(o.getLandOwnerId(),0,1,2026);
        assertEquals(0,new BigDecimal("1290").compareTo(total.getNetPayable()));
    }

    @Test void advancesAreDeductedOnlyOnceAndNegativeBalancesRemainVisible() {
        var o=owner("ownerA"); var f=factory("factoryA"); connect(o,f);
        Loan loan=new Loan(); loan.setLandOwner(o); loan.setFactoryOwner(f); loan.setLoanAmount(new BigDecimal("100"));
        loan.setApprovalDate(LocalDate.of(2025,12,10)); loan.setApprovalStatus(Loan.ApprovalStatus.Approved); loan.setRequestType(Loan.RequestType.ADVANCE); loans.save(loan);
        assertEquals(0,new BigDecimal("100").compareTo(bills.preview(o.getLandOwnerId(),f.getFactoryOwnerId(),1,2026).getLoanDeductions()));
        assertEquals(0,new BigDecimal("-100").compareTo(bills.preview(o.getLandOwnerId(),f.getFactoryOwnerId(),1,2026).getNetPayable()));
        assertEquals(0,BigDecimal.ZERO.compareTo(bills.preview(o.getLandOwnerId(),f.getFactoryOwnerId(),2,2026).getLoanDeductions()));
        assertEquals(0,BigDecimal.ZERO.compareTo(bills.preview(o.getLandOwnerId(),f.getFactoryOwnerId(),12,2025).getLoanDeductions()));
    }

    @Test void historicalDeliveryRatesArePreservedAndFutureRatesAreNotUsed() {
        var o=owner("ownerA"); var f=factory("factoryA"); connect(o,f);
        TeaRateRequestDTO rate=new TeaRateRequestDTO(); rate.setFactoryOwnerId(f.getFactoryOwnerId()); rate.setRatePerKg(new BigDecimal("99")); rate.setEffectiveFrom(LocalDate.now().plusDays(5)); deliveries.setRate(rate);
        var d=delivery(o,f,LocalDate.now(),null);
        assertEquals(0,new BigDecimal("50").compareTo(d.getRatePerKg()));
        rate.setRatePerKg(new BigDecimal("60")); rate.setEffectiveFrom(LocalDate.now()); deliveries.setRate(rate);
        var updated=deliveries.updateDeliveryWeight(d.getDeliveryId(),new BigDecimal("20"));
        assertEquals(0,new BigDecimal("50").compareTo(updated.getRatePerKg()));
        assertEquals(1,payments.count());
    }

    @Test void loanApprovalChequeTransitionsAndSettlementPersist() throws Exception {
        var o=owner("ownerA"); var f=factory("factoryA"); connect(o,f);
        var ownerSession=login("ownerA"); var factorySession=login("factoryA");
        mvc.perform(post("/api/loans").session(ownerSession).with(csrf()).contentType("application/json")
            .content("{\"landOwnerId\":"+o.getLandOwnerId()+",\"factoryOwnerId\":"+f.getFactoryOwnerId()+",\"loanAmount\":100,\"reason\":\"Supplies\",\"requestType\":\"LOAN\"}"))
            .andExpect(status().isCreated());
        int loan=loans.findAll().getFirst().getLoanId();
        mvc.perform(put("/api/loans/"+loan+"/status").session(factorySession).with(csrf()).contentType("application/json").content("{\"approvalStatus\":\"Approved\"}"))
            .andExpect(status().isOk());
        for(int i=0;i<2;i++) mvc.perform(post("/api/cheques").session(factorySession).with(csrf()).contentType("application/json").content("{\"loanId\":"+loan+"}"))
            .andExpect(status().isCreated());
        assertEquals(1,cheques.count()); int cheque=cheques.findAll().getFirst().getChequeId();
        mvc.perform(put("/api/cheques/"+cheque+"/verification-status").session(ownerSession).with(csrf()).contentType("application/json").content("{\"status\":\"Verified\"}"))
            .andExpect(status().isBadRequest());
        for(String state:new String[]{"In_Transit","Delivered"}) mvc.perform(put("/api/cheques/"+cheque+"/delivery-status").session(factorySession).with(csrf()).contentType("application/json").content("{\"status\":\""+state+"\"}"))
            .andExpect(status().isOk());
        mvc.perform(put("/api/cheques/"+cheque+"/verification-status").session(ownerSession).with(csrf()).contentType("application/json").content("{\"status\":\"Verified\"}"))
            .andExpect(status().isOk());
        delivery(o,f,LocalDate.now(),"50"); YearMonth now=YearMonth.now();
        String settlement="{\"landOwnerId\":"+o.getLandOwnerId()+",\"month\":"+now.getMonthValue()+",\"year\":"+now.getYear()+",\"method\":\"BANK_TRANSFER\",\"reference\":\"EXTERNAL-123\"}";
        for(int i=0;i<2;i++) mvc.perform(post("/api/payments/factory/"+f.getFactoryOwnerId()+"/settlements").session(factorySession).with(csrf()).contentType("application/json").content(settlement)).andExpect(status().isOk());
        mvc.perform(get("/api/payments/factory/"+f.getFactoryOwnerId()).session(factorySession)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("Processed"));
    }
}
