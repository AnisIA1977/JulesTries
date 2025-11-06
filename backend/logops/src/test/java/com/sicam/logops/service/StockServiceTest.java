package com.sicam.logops.service;

import com.sicam.logops.model.Materiel;
import com.sicam.logops.repository.MaterielRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private MaterielRepository materielRepository;

    @InjectMocks
    private StockService stockService;

    private Materiel materiel;

    @BeforeEach
    void setUp() {
        materiel = new Materiel();
        materiel.setId(1L);
        materiel.setQuantity(50);
        materiel.setSeuilAlerte(10);
    }

    @Test
    void testGetStockLevel() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        int stockLevel = stockService.getStockLevel(1L);
        assertEquals(50, stockLevel);
    }

    @Test
    void testUpdateStockLevel() {
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        when(materielRepository.save(any(Materiel.class))).thenAnswer(i -> i.getArguments()[0]);
        Materiel updatedMateriel = stockService.updateStockLevel(1L, 60);
        assertEquals(60, updatedMateriel.getQuantity());
    }

    @Test
    void testIsStockBelowAlertThreshold() {
        materiel.setQuantity(5);
        when(materielRepository.findById(1L)).thenReturn(Optional.of(materiel));
        assertTrue(stockService.isStockBelowAlertThreshold(1L));
    }
}
