package com.sicam.logops.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "logistic_requests")
public class LogisticRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String requestIdStr;
    private String description;
    @ManyToOne
    private NavalUnit unit;
    private String priority;
    private String status;
    private LocalDate dueDate;
}
