package cl.kioskonube.notificaciones.dto;

import java.time.Instant;

/**
 * Respuesta de POST /notificaciones: la notificación quedó en la cola.
 */
public record NotificacionEncoladaResponse(
        String id,
        String estado,
        Instant fechaCreacion
) {
}
