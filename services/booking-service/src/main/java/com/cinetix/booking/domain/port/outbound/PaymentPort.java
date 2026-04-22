package com.cinetix.booking.domain.port.outbound;

import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.valueobject.Money;

import java.util.function.Consumer;

public interface PaymentPort {

    PaymentResult initiatePayment(BookingId bookingId, CustomerId customerId,
                                   Money amount, String method);

    void refundPayment(PaymentId paymentId, String reason);

    sealed interface PaymentResult permits PaymentResult.Success, PaymentResult.Failure {
        record Success(String paymentId, String paymentUrl, String pspProvider)
            implements PaymentResult {}
        record Failure(String errorCode, String message)
            implements PaymentResult {}

        static PaymentResult success(String paymentId, String paymentUrl, String psp) {
            return new Success(paymentId, paymentUrl, psp);
        }
        static PaymentResult failure(String code, String message) {
            return new Failure(code, message);
        }

        default void ifSuccessOrElse(Consumer<Success> onSuccess, Consumer<Failure> onFailure) {
            if (this instanceof Success s) onSuccess.accept(s);
            else if (this instanceof Failure f) onFailure.accept(f);
        }
    }
}
