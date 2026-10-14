package cl.kioskonube.notificaciones.messaging;

import java.time.Instant;

/**
 * Mensaje que viaja por RabbitMQ (se serializa a JSON).
 */
public record NotificacionMessage(
        String id,
        String destinatario,
        String asunto,
        String mensaje,
        String origen,
        Instant fechaCreacion
) {
}