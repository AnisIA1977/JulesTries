package com.sicam.logops.service;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.model.Prevision;
import com.sicam.logops.repository.MaterielRepository;
import com.sicam.logops.repository.PrevisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PrevisionService {

    private final PrevisionRepository previsionRepository;
    private final MaterielRepository materielRepository;

    private static final double ALPHA = 0.5;

    @Autowired
    public PrevisionService(PrevisionRepository previsionRepository, MaterielRepository materielRepository) {
        this.previsionRepository = previsionRepository;
        this.materielRepository = materielRepository;
    }

    public Prevision createOrUpdatePrevision(Long materielId, int annee, int consommationReelleAnneePrecedente) {
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));

        Optional<Prevision> previsionAnneePrecedenteOpt = previsionRepository.findByMaterielIdAndAnnee(materielId, annee - 1);

        int quantitePrevue;

        if (previsionAnneePrecedenteOpt.isPresent()) {
            Prevision previsionAnneePrecedente = previsionAnneePrecedenteOpt.get();
            // Pn = Pn-1 + α (Rn-1 – Pn-1)
            quantitePrevue = (int) (previsionAnneePrecedente.getQuantitePrevue() + ALPHA * (consommationReelleAnneePrecedente - previsionAnneePrecedente.getQuantitePrevue()));
        } else {
            // If no previous forecast, we start with the alert threshold as per RG19
            quantitePrevue = materiel.getSeuilAlerte();
        }

        // RG19: Pn >= Sa
        if (quantitePrevue < materiel.getSeuilAlerte()) {
            quantitePrevue = materiel.getSeuilAlerte();
        }

        // Update the real consumption for the previous year's forecast if it exists
        previsionAnneePrecedenteOpt.ifPresent(prevision -> {
            prevision.setConsommationReelle(consommationReelleAnneePrecedente);
            previsionRepository.save(prevision);
        });

        Optional<Prevision> previsionActuelleOpt = previsionRepository.findByMaterielIdAndAnnee(materielId, annee);

        Prevision previsionActuelle;
        if(previsionActuelleOpt.isPresent()){
            previsionActuelle = previsionActuelleOpt.get();
            previsionActuelle.setQuantitePrevue(quantitePrevue);
        } else {
            previsionActuelle = Prevision.builder()
                    .materiel(materiel)
                    .annee(annee)
                    .quantitePrevue(quantitePrevue)
                    .consommationReelle(0) // Real consumption for the current year is not yet known
                    .build();
        }


        return previsionRepository.save(previsionActuelle);
    }
}
