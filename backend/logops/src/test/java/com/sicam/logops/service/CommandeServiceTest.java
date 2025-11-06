package com.sicam.logops.service;

import com.sicam.logops.model.Commande;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.CommandeRepository;
import com.sicam.logops.repository.MaterielRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private MaterielRepository materielRepository;

    @InjectMocks
    private CommandeService commandeService;

    private Materiel materiel;
    private Commande commande;

    @BeforeEach
    void setUp() {
        materiel = new Materiel();
        materiel.setId(1L);
        materiel.setQuantity(50);
        materiel.setSeuilAlerte(10);

        commande = new Commande();
        commande.setId(1L);
        commande.setMateriels(new ArrayList<>());
    }

    @Test
    void testCreateCommande() {
        when(materielRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(materiel));
        when(commandeRepository.save(any(Commande.class))).thenAnswer(i -> i.getArguments()[0]);

        Commande createdCommande = commandeService.createCommande(Collections.singletonList(1L));

        assertEquals("CREATED", createdCommande.getStatus());
        assertEquals(1, createdCommande.getMateriels().size());
    }

    @Test
    void testUpdateCommandeStatus() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(commandeRepository.save(any(Commande.class))).thenAnswer(i -> i.getArguments()[0]);

        Commande updatedCommande = commandeService.updateCommandeStatus(1L, "SHIPPED");

        assertEquals("SHIPPED", updatedCommande.getStatus());
    }

    @Test
    void testAddMaterielToCommande() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(commandeRepository.save(any(Commande.class))).thenAnswer(i -> i.getArguments()[0]);

        Commande updatedCommande = commandeService.addMaterielToCommande(1L, 1L);

        assertEquals(1, updatedCommande.getMateriels().size());
    }

    @Test
    void testAddMaterielToCommande_ExceedsSmax() {
        materiel.setQuantity(0);
        materiel.setSeuilAlerte(0); // This makes Smax = 0
        commande.getMateriels().add(materiel); // This makes the count 1
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));

        assertThrows(RuntimeException.class, () -> {
            // The check is count + 1 > Smax, which is 1 + 1 > 0, so it should throw
            commandeService.addMaterielToCommande(1L, 1L);
        });
    }
}
