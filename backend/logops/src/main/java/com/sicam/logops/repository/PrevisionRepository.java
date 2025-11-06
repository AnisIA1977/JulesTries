package com.sicam.logops.repository;

import com.sicam.logops.model.Prevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrevisionRepository extends JpaRepository<Prevision, Long> {
    Optional<Prevision> findByMaterielIdAndAnnee(Long materielId, int annee);
}
