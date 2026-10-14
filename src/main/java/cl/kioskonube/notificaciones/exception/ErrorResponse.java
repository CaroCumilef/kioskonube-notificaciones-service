package cl.kioskonube.notificaciones.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        List<String> details
) {
}