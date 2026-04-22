package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record BookingId(UUID value) implements Serializable {
    public static BookingId of(UUID value)  { return new BookingId(value); }
    public static BookingId generate()      { return new BookingId(UUID.randomUUID()); }
    @Override public String toString()      { return value.toString(); }
}
