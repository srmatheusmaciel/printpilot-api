package br.com.printpilot.repository;

import br.com.printpilot.entity.AreaPricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaPricingRuleRepository extends JpaRepository<AreaPricingRule, Long> {

    Optional<AreaPricingRule> findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
