package com.sicam.logops.service;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.MaterielRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final MaterielRepository materielRepository;

    @Autowired
    public StockService(MaterielRepository materielRepository) {
        this.materielRepository = materielRepository;
    }

    public int getStockLevel(Long materielId) {
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));
        return materiel.getQuantity();
    }

    public Materiel updateStockLevel(Long materielId, int newQuantity) {
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));
        materiel.setQuantity(newQuantity);
        return materielRepository.save(materiel);
    }

    public boolean isStockBelowAlertThreshold(Long materielId) {
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));
        // RG3: Check if stock has reached the alert threshold (Sa)
        return materiel.getQuantity() <= materiel.getSeuilAlerte();
    }
}
