package com.sicam.logops.controller;

import com.sicam.logops.model.Distribution;
import com.sicam.logops.service.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distributions")
public class DistributionController {

    private final DistributionService distributionService;

    @Autowired
    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    @PostMapping
    public ResponseEntity<Distribution> distributeMateriel(@RequestParam Long materielId, @RequestParam int quantity, @RequestParam String unit) {
        Distribution distribution = distributionService.distributeMateriel(materielId, quantity, unit);
        return ResponseEntity.ok(distribution);
    }
}
