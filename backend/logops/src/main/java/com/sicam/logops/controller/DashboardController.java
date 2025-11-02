package com.sicam.logops.controller;

import com.sicam.logops.dto.DashboardData;
import com.sicam.logops.model.*;
import com.sicam.logops.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private NavalUnitRepository navalUnitRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private KpiRepository kpiRepository;
    @Autowired
    private LogisticRequestRepository logisticRequestRepository;

    @GetMapping
    public DashboardData getDashboardData() {
        Map<String, Long> unitStatus = navalUnitRepository.findAll().stream()
                .collect(Collectors.groupingBy(NavalUnit::getStatus, Collectors.counting()));
        List<Resource> criticalResources = resourceRepository.findAll();
        List<Alert> priorityAlerts = alertRepository.findAll();
        List<Kpi> kpis = kpiRepository.findAll();
        List<LogisticRequest> logisticRequests = logisticRequestRepository.findAll();

        return new DashboardData(unitStatus, criticalResources, priorityAlerts, kpis, logisticRequests);
    }
}
