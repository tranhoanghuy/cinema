package com.cinetix.showtime.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record ShowtimeId(UUID value) implements Serializable {
    public ShowtimeId { if (value == null) throw new IllegalArgumentException("ShowtimeId cannot be null"); }
    public static ShowtimeId of(UUID value)   { return new ShowtimeId(value); }
    public static ShowtimeId of(String value) { return new ShowtimeId(UUID.fromString(value)); }
    public static ShowtimeId generate()       { return new ShowtimeId(UUID.randomUUID()); }
    @Override public String toString()        { return value.toString(); }
}
