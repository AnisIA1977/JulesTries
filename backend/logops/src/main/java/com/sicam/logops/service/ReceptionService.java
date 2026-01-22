package com.sicam.logops.service;

import com.sicam.logops.model.Commande;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.model.Reception;
import com.sicam.logops.repository.CommandeRepository;
import com.sicam.logops.repository.MaterielRepository;
import com.sicam.logops.repository.ReceptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceptionService {

    private final ReceptionRepository receptionRepository;
    private final CommandeRepository commandeRepository;
    private final MaterielRepository materielRepository;

    @Autowired
    public ReceptionService(ReceptionRepository receptionRepository, CommandeRepository commandeRepository, MaterielRepository materielRepository) {
        this.receptionRepository = receptionRepository;
        this.commandeRepository = commandeRepository;
        this.materielRepository = materielRepository;
    }

    @Transactional
    public Reception createReception(Long commandeId, List<Long> materielIds, List<Integer> quantities) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande not found with id: " + commandeId));

        List<Materiel> receivedMateriels = materielRepository.findAllById(materielIds);

        // RG041: Check for conformity
        List<Long> orderedMaterielIds = commande.getMateriels().stream().map(Materiel::getId).collect(Collectors.toList());
        if (!orderedMaterielIds.containsAll(materielIds)) {
            throw new RuntimeException("Received materiel is not compliant with the order.");
        }

        Reception reception = Reception.builder()
                .commande(commande)
                .date(LocalDate.now())
                .status("RECEIVED")
                .materiels(receivedMateriels)
                .build();

        // Update stock quantities
        for (int i = 0; i < receivedMateriels.size(); i++) {
            Materiel materiel = receivedMateriels.get(i);
            materiel.setQuantity(materiel.getQuantity() + quantities.get(i));
            materielRepository.save(materiel);
        }

        return receptionRepository.save(reception);
    }
}
