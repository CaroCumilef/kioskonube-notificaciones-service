package cl.kioskonube.notificaciones.messaging;

import cl.kioskonube.notificaciones.config.MensajeriaProperties;
import cl.kioskonube.notificaciones.exception.PublicacionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor: publica notificaciones en notificaciones.exchange.
 * Es la única clase que conoce RabbitTemplate.
 */
@Component
public class NotificacionPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties props;

    public NotificacionPublisher(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicar(NotificacionMessage mensaje) {
        try {
            rabbitTemplate.convertAndSend(props.exchange(), props.routingKey(), mensaje);
            log.info("Notificación publicada id={} exchange={} routingKey={}",
                    mensaje.id(), props.exchange(), props.routingKey());
        } catch (AmqpException e) {
            log.error("No se pudo publicar la notificación id={}: {}", mensaje.id(), e.getMessage());
            throw new PublicacionException("No se pudo publicar la notificación en RabbitMQ", e);
        }
    }
}