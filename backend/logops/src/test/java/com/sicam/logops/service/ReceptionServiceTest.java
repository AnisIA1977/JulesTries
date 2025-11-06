package com.sicam.logops.service;

import com.sicam.logops.model.Commande;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.model.Reception;
import com.sicam.logops.repository.CommandeRepository;
import com.sicam.logops.repository.MaterielRepository;
import com.sicam.logops.repository.ReceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceptionServiceTest {

    @Mock
    private ReceptionRepository receptionRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private MaterielRepository materielRepository;

    @InjectMocks
    private ReceptionService receptionService;

    private Materiel materiel;
    private Commande commande;

    @BeforeEach
    void setUp() {
        materiel = new Materiel();
        materiel.setId(1L);
        materiel.setQuantity(50);

        commande = new Commande();
        commande.setId(1L);
        commande.setMateriels(Collections.singletonList(materiel));
    }

    @Test
    void testCreateReception() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(materielRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(materiel));
        when(receptionRepository.save(any(Reception.class))).thenAnswer(i -> i.getArguments()[0]);

        Reception reception = receptionService.createReception(1L, Collections.singletonList(1L), Collections.singletonList(1));

        assertEquals("RECEIVED", reception.getStatus());
        assertEquals(51, materiel.getQuantity());
    }

    @Test
    void testCreateReception_NonCompliant() {
        Commande anotherCommande = new Commande();
        anotherCommande.setId(2L);
        anotherCommande.setMateriels(Collections.emptyList());

        when(commandeRepository.findById(1L)).thenReturn(Optional.of(anotherCommande));
        when(materielRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(materiel));

        assertThrows(RuntimeException.class, () -> {
            receptionService.createReception(1L, Collections.singletonList(1L), Collections.singletonList(1));
        });
    }
}
