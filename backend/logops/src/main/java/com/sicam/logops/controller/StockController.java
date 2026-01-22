package com.sicam.logops.controller;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{materielId}")
    public ResponseEntity<Integer> getStockLevel(@PathVariable Long materielId) {
        int stockLevel = stockService.getStockLevel(materielId);
        return ResponseEntity.ok(stockLevel);
    }

    @PutMapping("/{materielId}")
    public ResponseEntity<Materiel> updateStockLevel(@PathVariable Long materielId, @RequestParam int newQuantity) {
        Materiel materiel = stockService.updateStockLevel(materielId, newQuantity);
        return ResponseEntity.ok(materiel);
    }

    @GetMapping("/{materielId}/alert")
    public ResponseEntity<Boolean> isStockBelowAlertThreshold(@PathVariable Long materielId) {
        boolean isBelowAlert = stockService.isStockBelowAlertThreshold(materielId);
        return ResponseEntity.ok(isBelowAlert);
    }
}
