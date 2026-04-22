package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record CustomerId(UUID value) implements Serializable {
    public static CustomerId of(UUID value) { return new CustomerId(value); }
    @Override public String toString()      { return value.toString(); }
}
