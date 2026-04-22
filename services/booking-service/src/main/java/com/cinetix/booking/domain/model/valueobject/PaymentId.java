package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record PaymentId(String value) implements Serializable {
    public static PaymentId of(String value) { return new PaymentId(value); }
    @Override public String toString()       { return value; }
}
