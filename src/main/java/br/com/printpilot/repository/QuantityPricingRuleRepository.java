package br.com.printpilot.repository;

import br.com.printpilot.entity.QuantityPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuantityPricingRuleRepository extends JpaRepository<QuantityPricingRule, Long> {
    Optional<QuantityPricingRule> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
}
