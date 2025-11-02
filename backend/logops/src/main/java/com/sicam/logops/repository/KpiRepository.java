package com.sicam.logops.repository;

import com.sicam.logops.model.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiRepository extends JpaRepository<Kpi, Long> {
}
