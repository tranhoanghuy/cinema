package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record SeatId(UUID value) implements Serializable {
    public static SeatId of(UUID value)    { return new SeatId(value); }
    public static SeatId of(String value)  { return new SeatId(UUID.fromString(value)); }
    @Override public String toString()     { return value.toString(); }
}
