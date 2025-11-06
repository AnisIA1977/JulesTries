package com.sicam.logops.controller;

import com.sicam.logops.model.Commande;
import com.sicam.logops.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    @Autowired
    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public ResponseEntity<Commande> createCommande(@RequestParam List<Long> materielIds) {
        Commande commande = commandeService.createCommande(materielIds);
        return ResponseEntity.ok(commande);
    }

    @PutMapping("/{commandeId}/status")
    public ResponseEntity<Commande> updateCommandeStatus(@PathVariable Long commandeId, @RequestParam String status) {
        Commande commande = commandeService.updateCommandeStatus(commandeId, status);
        return ResponseEntity.ok(commande);
    }

    @PostMapping("/{commandeId}/materiels")
    public ResponseEntity<Commande> addMaterielToCommande(@PathVariable Long commandeId, @RequestParam Long materielId) {
        Commande commande = commandeService.addMaterielToCommande(commandeId, materielId);
        return ResponseEntity.ok(commande);
    }

    @GetMapping("/{commandeId}/purchase-order")
    public ResponseEntity<String> generatePurchaseOrder(@PathVariable Long commandeId) {
        String purchaseOrder = commandeService.generatePurchaseOrder(commandeId);
        return ResponseEntity.ok(purchaseOrder);
    }
}
