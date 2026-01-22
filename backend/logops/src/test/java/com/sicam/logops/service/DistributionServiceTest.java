package com.sicam.logops.service;

import com.sicam.logops.model.Distribution;
import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.DistributionRepository;
import com.sicam.logops.repository.MaterielRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributionServiceTest {

    @Mock
    private DistributionRepository distributionRepository;

    @Mock
    private MaterielRepository materielRepository;

    @InjectMocks
    private DistributionService distributionService;

    private Materiel materiel;

    @BeforeEach
    void setUp() {
        materiel = new Materiel();
        materiel.setId(1L);
        materiel.setQuantity(50);
        materiel.setSeuilSecurite(5);
    }

    @Test
    void testDistributeMateriel() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(distributionRepository.save(any(Distribution.class))).thenAnswer(i -> i.getArguments()[0]);

        Distribution distribution = distributionService.distributeMateriel(1L, 10, "Unit A");

        assertEquals(40, materiel.getQuantity());
        assertEquals("Unit A", distribution.getUnit());
    }

    @Test
    void testDistributeMateriel_BelowSecurityThreshold() {
        materiel.setQuantity(5);
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));

        assertThrows(RuntimeException.class, () -> {
            distributionService.distributeMateriel(1L, 1, "Unit A");
        });
    }

    @Test
    void testDistributeMateriel_InsufficientStock() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));

        assertThrows(RuntimeException.class, () -> {
            distributionService.distributeMateriel(1L, 60, "Unit A");
        });
    }
}
