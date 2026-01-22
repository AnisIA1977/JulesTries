package com.sicam.logops.controller;

import com.sicam.logops.model.Prevision;
import com.sicam.logops.service.PrevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/previsions")
public class PrevisionController {

    private final PrevisionService previsionService;

    @Autowired
    public PrevisionController(PrevisionService previsionService) {
        this.previsionService = previsionService;
    }

    @PostMapping
    public ResponseEntity<Prevision> createOrUpdatePrevision(@RequestParam Long materielId, @RequestParam int annee, @RequestParam int consommationReelleAnneePrecedente) {
        Prevision prevision = previsionService.createOrUpdatePrevision(materielId, annee, consommationReelleAnneePrecedente);
        return ResponseEntity.ok(prevision);
    }
}
