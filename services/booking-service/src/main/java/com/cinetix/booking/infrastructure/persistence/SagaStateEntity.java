package com.cinetix.booking.infrastructure.persistence;

import com.cinetix.booking.domain.model.SagaStep;
import com.cinetix.booking.infrastructure.persistence.converter.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "saga_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaStateEntity {

    @Id
    @Column(name = "booking_id")
    private UUID bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 50)
    private SagaStep currentStep;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "compensation_reason")
    private String compensationReason;

    @Column(name = "step_history", columnDefinition = "jsonb")
    @Convert(converter = JsonStringListConverter.class)
    private List<String> stepHistory = new ArrayList<>();

    public static SagaStateEntity create(UUID bookingId) {
        var s = new SagaStateEntity();
        s.bookingId   = bookingId;
        s.currentStep = SagaStep.HOLD_SEATS;
        s.startedAt   = Instant.now();
        s.updatedAt   = Instant.now();
        s.stepHistory = new ArrayList<>();
        return s;
    }

    public void advance(SagaStep next, String compensationReason) {
        if (this.currentStep != null) {
            this.stepHistory.add(this.currentStep.name());
        }
        this.currentStep       = next;
        this.updatedAt         = Instant.now();
        this.compensationReason = compensationReason;
        if (next == SagaStep.COMPLETED || next == SagaStep.COMPENSATED) {
            this.completedAt = Instant.now();
        }
    }
}
