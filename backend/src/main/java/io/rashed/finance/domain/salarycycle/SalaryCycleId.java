package io.rashed.finance.domain.salarycycle;

import java.util.UUID;
import io.rashed.finance.common.valueobject.EntityId;

/**
 * Strongly typed identifier for SalaryCycle aggregate.
 */
public final class SalaryCycleId extends EntityId {

    private SalaryCycleId(UUID value) {
        super(value);
    }

    public static SalaryCycleId newId() {
        return new SalaryCycleId(UUID.randomUUID());
    }

    public static SalaryCycleId of(UUID value) {
        return new SalaryCycleId(value);
    }

    public static SalaryCycleId of(String value) {
        return new SalaryCycleId(UUID.fromString(value));
    }
}