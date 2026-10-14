package cl.kioskonube.notificaciones.service;

import cl.kioskonube.notificaciones.dto.NotificacionEncoladaResponse;
import cl.kioskonube.notificaciones.dto.NotificacionRequest;
import cl.kioskonube.notificaciones.messaging.NotificacionMessage;
import cl.kioskonube.notificaciones.messaging.NotificacionPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Lógica de negocio: arma la notificación y la delega al productor.
 * No conoce RabbitMQ.
 */
@Service
public class NotificacionService {

    private static final String ORIGEN_POR_DEFECTO = "notificaciones-service";

    private final NotificacionPublisher publisher;

    public NotificacionService(NotificacionPublisher publisher) {
        this.publisher = publisher;
    }

    public NotificacionEncoladaResponse encolar(NotificacionRequest request) {
        String origen = (request.origen() == null || request.origen().isBlank())
                ? ORIGEN_POR_DEFECTO
                : request.origen().trim();

        NotificacionMessage mensaje = new NotificacionMessage(
                UUID.randomUUID().toString(),
                request.destinatario().trim(),
                request.asunto().trim(),
                request.mensaje(),
                origen,
                Instant.now()
        );

        publisher.publicar(mensaje);

        return new NotificacionEncoladaResponse(mensaje.id(), "ENCOLADA", mensaje.fechaCreacion());
    }
}