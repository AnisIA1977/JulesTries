package com.sicam.logops.controller;

import com.sicam.logops.model.Reception;
import com.sicam.logops.service.ReceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/receptions")
public class ReceptionController {

    private final ReceptionService receptionService;

    @Autowired
    public ReceptionController(ReceptionService receptionService) {
        this.receptionService = receptionService;
    }

    @PostMapping
    public ResponseEntity<Reception> createReception(@RequestParam Long commandeId, @RequestParam List<Long> materielIds, @RequestParam List<Integer> quantities) {
        Reception reception = receptionService.createReception(commandeId, materielIds, quantities);
        return ResponseEntity.ok(reception);
    }
}
