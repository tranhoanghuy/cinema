package com.cinetix.booking.domain.model.valueobject;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record VoucherCode(String value) implements Serializable {
    public VoucherCode {
        if (value != null) value = value.trim().toUpperCase();
    }
    public static VoucherCode of(String value) { return new VoucherCode(value); }
    @Override public String toString()         { return value; }
}
