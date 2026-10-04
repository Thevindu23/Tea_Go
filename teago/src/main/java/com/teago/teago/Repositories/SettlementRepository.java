package com.teago.teago.Repositories;
import com.teago.teago.models.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SettlementRepository extends JpaRepository<Settlement,Integer> {
    Optional<Settlement> findByFactory_FactoryOwnerIdAndOwner_LandOwnerIdAndMonthAndYear(Integer factory,Integer owner,Integer month,Integer year);
}
