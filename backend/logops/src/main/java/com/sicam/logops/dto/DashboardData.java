package com.sicam.logops.dto;

import com.sicam.logops.model.NavalUnit;
import com.sicam.logops.model.Resource;
import com.sicam.logops.model.Alert;
import com.sicam.logops.model.Kpi;
import com.sicam.logops.model.LogisticRequest;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardData {
    private Map<String, Long> unitStatus;
    private List<Resource> criticalResources;
    private List<Alert> priorityAlerts;
    private List<Kpi> kpis;
    private List<LogisticRequest> logisticRequests;
}
