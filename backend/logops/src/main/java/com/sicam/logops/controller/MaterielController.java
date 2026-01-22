package com.sicam.logops.controller;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.MaterielRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materiels")
public class MaterielController {

    private final MaterielRepository materielRepository;

    @Autowired
    public MaterielController(MaterielRepository materielRepository) {
        this.materielRepository = materielRepository;
    }

    @GetMapping
    public ResponseEntity<List<Materiel>> getAllMateriels() {
        List<Materiel> materiels = materielRepository.findAll();
        return ResponseEntity.ok(materiels);
    }

    @PostMapping
    public ResponseEntity<Materiel> createMateriel(@RequestBody Materiel materiel) {
        Materiel savedMateriel = materielRepository.save(materiel);
        return ResponseEntity.ok(savedMateriel);
    }
}
