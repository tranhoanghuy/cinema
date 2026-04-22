package com.cinetix.notification.application;

import com.cinetix.notification.domain.model.NotificationLog;
import com.cinetix.notification.infrastructure.persistence.NotificationLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final JavaMailSender                mailSender;
    private final NotificationLogJpaRepository  logRepo;

    public void sendBookingConfirmation(UUID customerId, String email, String bookingId,
                                         String movieTitle, String cinemaName, String showtimeStr) {
        String subject = "[CineTix] Đặt vé thành công - " + movieTitle;
        String body = String.format(
            "Xin chào,\n\nĐặt vé của bạn đã được xác nhận!\n\n" +
            "Phim: %s\nRạp: %s\nSuất chiếu: %s\nMã đặt vé: %s\n\n" +
            "Vui lòng kiểm tra vé trong ứng dụng CineTix.\n\nCảm ơn bạn đã sử dụng dịch vụ!",
            movieTitle, cinemaName, showtimeStr, bookingId
        );
        sendEmail(customerId, email, subject, body, "BOOKING_CONFIRMED", bookingId);
    }

    public void sendBookingCancellation(UUID customerId, String email, String bookingId, String reason) {
        String subject = "[CineTix] Vé đã bị hủy";
        String body = String.format(
            "Xin chào,\n\nĐặt vé %s của bạn đã bị hủy.\nLý do: %s\n\n" +
            "Nếu bạn đã thanh toán, hoàn tiền sẽ được xử lý trong 3-5 ngày làm việc.\n\nCảm ơn!",
            bookingId, reason
        );
        sendEmail(customerId, email, subject, body, "BOOKING_CANCELLED", bookingId);
    }

    public void sendPaymentFailed(UUID customerId, String email, String bookingId, String reason) {
        String subject = "[CineTix] Thanh toán thất bại";
        String body = String.format(
            "Xin chào,\n\nThanh toán cho đặt vé %s thất bại.\nLý do: %s\n\n" +
            "Vui lòng thử lại hoặc liên hệ hỗ trợ.", bookingId, reason
        );
        sendEmail(customerId, email, subject, body, "PAYMENT_FAILED", bookingId);
    }

    private void sendEmail(UUID recipientId, String email, String subject, String body,
                            String type, String referenceId) {
        NotificationLog log = NotificationLog.builder()
            .recipientId(recipientId)
            .recipientEmail(email)
            .channel("EMAIL")
            .type(type)
            .subject(subject)
            .referenceId(referenceId)
            .build();

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject(subject);
            msg.setText(body);
            msg.setFrom("noreply@cinetix.vn");
            mailSender.send(msg);
            log.markSent();
            logRepo.save(log);
            this.log.info("Email sent: type={} to={} ref={}", type, email, referenceId);
        } catch (Exception e) {
            log.markFailed(e.getMessage());
            logRepo.save(log);
            this.log.error("Email failed: type={} to={} error={}", type, email, e.getMessage());
        }
    }
}
