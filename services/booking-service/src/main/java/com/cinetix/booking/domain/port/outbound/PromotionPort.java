package com.cinetix.booking.domain.port.outbound;

import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.valueobject.Money;

import java.util.function.Consumer;

public interface PromotionPort {

    VoucherResult validateVoucher(VoucherCode code, CustomerId customerId,
                                   Money orderAmount, ShowtimeId showtimeId);

    void redeemVoucher(VoucherCode code, BookingId bookingId, CustomerId customerId);

    void voidVoucher(VoucherCode code, BookingId bookingId);

    sealed interface VoucherResult permits VoucherResult.Valid, VoucherResult.Invalid {
        record Valid(String voucherId, String promotionName,
                     Money discountAmount, Money finalAmount)
            implements VoucherResult {}
        record Invalid(String errorCode, String message)
            implements VoucherResult {}

        static VoucherResult valid(String voucherId, String promoName,
                                    Money discount, Money finalAmount) {
            return new Valid(voucherId, promoName, discount, finalAmount);
        }
        static VoucherResult invalid(String code, String message) {
            return new Invalid(code, message);
        }

        default void ifSuccessOrElse(Consumer<Valid> onValid, Consumer<Invalid> onInvalid) {
            if (this instanceof Valid v) onValid.accept(v);
            else if (this instanceof Invalid i) onInvalid.accept(i);
        }
    }
}
