package com.sicam.logops.service;

import com.sicam.logops.model.Commande;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.CommandeRepository;
import com.sicam.logops.repository.MaterielRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final MaterielRepository materielRepository;

    @Autowired
    public CommandeService(CommandeRepository commandeRepository, MaterielRepository materielRepository) {
        this.commandeRepository = commandeRepository;
        this.materielRepository = materielRepository;
    }

    public Commande createCommande(List<Long> materielIds) {
        List<Materiel> materiels = materielRepository.findAllById(materielIds);
        Commande commande = Commande.builder()
                .date(LocalDate.now())
                .status("CREATED")
                .materiels(materiels)
                .build();
        return commandeRepository.save(commande);
    }

    public Commande updateCommandeStatus(Long commandeId, String status) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande not found with id: " + commandeId));
        commande.setStatus(status);
        return commandeRepository.save(commande);
    }

    public Commande addMaterielToCommande(Long commandeId, Long materielId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande not found with id: " + commandeId));
        Materiel materiel = materielRepository.findById(materielId)
                .orElseThrow(() -> new RuntimeException("Materiel not found with id: " + materielId));

        // RG30: Check if the ordered quantity exceeds the maximum stock level
        int Smax = (materiel.getQuantity() * 3) + materiel.getSeuilAlerte(); // Simplified Smax calculation
        if (commande.getMateriels().stream().filter(m -> m.getId().equals(materielId)).count() + 1 > Smax) {
            throw new RuntimeException("Ordered quantity exceeds the maximum stock level (Smax).");
        }

        commande.getMateriels().add(materiel);
        return commandeRepository.save(commande);
    }

    // This method is a placeholder for a more complex purchase order generation logic.
    public String generatePurchaseOrder(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande not found with id: " + commandeId));
        // In a real application, this would generate a PDF or a similar document.
        return "Purchase Order for Commande #" + commande.getId() + " created on " + commande.getDate();
    }
}
