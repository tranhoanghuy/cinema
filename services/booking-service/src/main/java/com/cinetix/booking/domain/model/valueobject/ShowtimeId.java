package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ShowtimeId(UUID value) implements Serializable {
    public static ShowtimeId of(UUID value) { return new ShowtimeId(value); }
    @Override public String toString()      { return value.toString(); }
}
