package com.sicam.logops.service;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.model.Prevision;
import com.sicam.logops.repository.MaterielRepository;
import com.sicam.logops.repository.PrevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrevisionServiceTest {

    @Mock
    private PrevisionRepository previsionRepository;

    @Mock
    private MaterielRepository materielRepository;

    @InjectMocks
    private PrevisionService previsionService;

    private Materiel materiel;

    @BeforeEach
    void setUp() {
        materiel = new Materiel();
        materiel.setId(1L);
        materiel.setSeuilAlerte(10);
    }

    @Test
    void testCreateOrUpdatePrevision_NoPreviousForecast() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(previsionRepository.findByMaterielIdAndAnnee(1L, 2022)).thenReturn(Optional.empty());
        when(previsionRepository.save(any(Prevision.class))).thenAnswer(i -> i.getArguments()[0]);

        Prevision prevision = previsionService.createOrUpdatePrevision(1L, 2023, 0);

        assertEquals(10, prevision.getQuantitePrevue());
    }

    @Test
    void testCreateOrUpdatePrevision_WithPreviousForecast() {
        Prevision previsionAnneePrecedente = new Prevision();
        previsionAnneePrecedente.setQuantitePrevue(20);

        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(previsionRepository.findByMaterielIdAndAnnee(1L, 2022)).thenReturn(Optional.of(previsionAnneePrecedente));
        when(previsionRepository.save(any(Prevision.class))).thenAnswer(i -> i.getArguments()[0]);

        Prevision prevision = previsionService.createOrUpdatePrevision(1L, 2023, 15);

        assertEquals(17, prevision.getQuantitePrevue()); // 20 + 0.5 * (15 - 20) = 17.5, rounded to 17
    }

    @Test
    void testCreateOrUpdatePrevision_BelowAlertThreshold() {
        materiel.setSeuilAlerte(20);
        Prevision previsionAnneePrecedente = new Prevision();
        previsionAnneePrecedente.setQuantitePrevue(15);

        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(previsionRepository.findByMaterielIdAndAnnee(1L, 2022)).thenReturn(Optional.of(previsionAnneePrecedente));
        when(previsionRepository.save(any(Prevision.class))).thenAnswer(i -> i.getArguments()[0]);

        Prevision prevision = previsionService.createOrUpdatePrevision(1L, 2023, 10);

        assertEquals(20, prevision.getQuantitePrevue());
    }
}
