package br.com.printpilot.repository;

import br.com.printpilot.entity.Finishing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinishingRepository extends JpaRepository<Finishing, Long> {
}
