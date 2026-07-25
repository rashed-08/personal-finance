package io.rashed.finance.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "salary_cycles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalaryCycleEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "cycle_name", nullable = false, length = 100)
    private String cycleName;

    @Column(name = "cycle_start_date", nullable = false)
    private LocalDate cycleStartDate;

    @Column(name = "cycle_end_date", nullable = false)
    private LocalDate cycleEndDate;

    @Column(name = "salary_received_date")
    private LocalDate salaryReceivedDate;

    @Column(name = "carry_forward_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal carryForwardAmount;

    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    @Column
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SalaryCycleEntity(
            UUID id,
            String cycleName,
            LocalDate cycleStartDate,
            LocalDate cycleEndDate,
            LocalDate salaryReceivedDate,
            BigDecimal carryForwardAmount,
            boolean closed,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.cycleName = cycleName;
        this.cycleStartDate = cycleStartDate;
        this.cycleEndDate = cycleEndDate;
        this.salaryReceivedDate = salaryReceivedDate;
        this.carryForwardAmount = carryForwardAmount;
        this.closed = closed;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
