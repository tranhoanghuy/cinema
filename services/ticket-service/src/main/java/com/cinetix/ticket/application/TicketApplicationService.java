package com.cinetix.ticket.application;

import com.cinetix.common.exception.ResourceNotFoundException;
import com.cinetix.ticket.domain.model.Ticket;
import com.cinetix.ticket.infrastructure.persistence.TicketJpaRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketApplicationService {

    private final TicketJpaRepository ticketRepo;
    private static final QRCodeWriter QR_WRITER = new QRCodeWriter();

    public record TicketIssueCommand(
        UUID bookingId, UUID customerId, UUID seatId, String seatCode,
        UUID showtimeId, String movieTitle, String cinemaName, String screenName,
        Instant showtimeStart, long unitPrice
    ) {}

    public List<Ticket> issueTickets(List<TicketIssueCommand> commands) {
        List<Ticket> tickets = new ArrayList<>();
        for (TicketIssueCommand cmd : commands) {
            String qrData = buildQrData(cmd.bookingId(), cmd.seatId());
            String qrCode = generateQrBase64(qrData);

            Ticket ticket = Ticket.builder()
                .bookingId(cmd.bookingId())
                .customerId(cmd.customerId())
                .seatId(cmd.seatId())
                .seatCode(cmd.seatCode())
                .showtimeId(cmd.showtimeId())
                .movieTitle(cmd.movieTitle())
                .cinemaName(cmd.cinemaName())
                .screenName(cmd.screenName())
                .showtimeStart(cmd.showtimeStart())
                .unitPrice(cmd.unitPrice())
                .qrData(qrData)
                .qrCode(qrCode)
                .build();

            tickets.add(ticketRepo.save(ticket));
        }
        log.info("Issued {} tickets for bookingId={}", tickets.size(),
            commands.isEmpty() ? "n/a" : commands.get(0).bookingId());
        return tickets;
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByBookingId(UUID bookingId) {
        return ticketRepo.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public List<Ticket> getByCustomerId(UUID customerId) {
        return ticketRepo.findByCustomerIdOrderByIssuedAtDesc(customerId);
    }

    public void cancelTicketsByBooking(UUID bookingId) {
        ticketRepo.findByBookingId(bookingId).forEach(t -> {
            t.cancel();
            ticketRepo.save(t);
        });
    }

    public Ticket validateAndUse(UUID ticketId) {
        Ticket ticket = ticketRepo.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        if (ticket.getStatus() != com.cinetix.ticket.domain.model.TicketStatus.ACTIVE)
            throw new IllegalStateException("Ticket is not active: " + ticket.getStatus());
        ticket.markUsed();
        return ticketRepo.save(ticket);
    }

    private String buildQrData(UUID bookingId, UUID seatId) {
        return "CINETIX:" + bookingId + ":" + seatId + ":" + System.currentTimeMillis();
    }

    private String generateQrBase64(String data) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = QR_WRITER.encode(data, BarcodeFormat.QR_CODE, 256, 256, hints);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("QR code generation failed for data={}: {}", data, e.getMessage());
            return "";
        }
    }
}
