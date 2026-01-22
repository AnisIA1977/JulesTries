package com.sicam.logops.service;

import com.sicam.logops.model.Distribution;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.DistributionRepository;
import com.sicam.logops.repository.MaterielRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DistributionService {

    private final DistributionRepository distributionRepository;
    private final MaterielRepository materielRepository;

    @Autowired
    public DistributionService(DistributionRepository distributionRepository, MaterielRepository materielRepository) {
        this.distributionRepository = distributionRepository;
        this.materielRepository = materielRepository;
    }

    @Transactional
    public Distribution distributeMateriel(Long materielId, int quantity, String unit) {
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));

        // RG17: At the security threshold, distribution of the item is blocked.
        if (materiel.getQuantity() <= materiel.getSeuilSecurite()) {
            throw new RuntimeException("Distribution is blocked as the stock has reached the security threshold.");
        }

        if (materiel.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for distribution.");
        }

        materiel.setQuantity(materiel.getQuantity() - quantity);
        materielRepository.save(materiel);

        Distribution distribution = Distribution.builder()
                .materiel(materiel)
                .quantity(quantity)
                .date(LocalDate.now())
                .unit(unit)
                .build();

        return distributionRepository.save(distribution);
    }
}
