package cl.kioskonube.notificaciones.messaging.consumer;

import cl.kioskonube.notificaciones.config.NotificacionesProperties;
import cl.kioskonube.notificaciones.exception.EnvioDefinitivoException;
import cl.kioskonube.notificaciones.exception.EnvioTemporalException;
import cl.kioskonube.notificaciones.messaging.NotificacionMessage;
import cl.kioskonube.notificaciones.service.EnvioNotificacionService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * Consumidor del dominio de notificaciones.
 * ACK manual: confirma solo cuando el envío funcionó.
 * Errores temporales: reintenta. Errores definitivos o reintentos agotados: NACK a la DLQ.
 */
@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    private final EnvioNotificacionService envioService;
    private final NotificacionesProperties props;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public NotificacionConsumer(EnvioNotificacionService envioService, NotificacionesProperties props) {
        this.envioService = envioService;
        this.props = props;
    }

    @RabbitListener(id = "notificaciones-consumer",
            queues = "${kioskonube.mensajeria.notificaciones.queue}")
    public void recibir(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        // 1. Convertir el JSON; si es ilegible, no tiene sentido reintentar
        NotificacionMessage notificacion;
        try {
            notificacion = jsonMapper.readValue(message.getBody(), NotificacionMessage.class);
        } catch (JacksonException e) {
            log.error("Mensaje ilegible (tag={}). NACK → DLQ. Error: {}", tag, e.getOriginalMessage());
            channel.basicNack(tag, false, false);
            return;
        }

        log.info("Mensaje recibido id={} destinatario={}", notificacion.id(), notificacion.destinatario());

        // 2. Intentar el envío con reintentos para errores temporales
        int maxIntentos = props.reintentos().maxIntentos();
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                envioService.enviar(notificacion);
                channel.basicAck(tag, false);
                log.info("ACK id={} (intento {}/{})", notificacion.id(), intento, maxIntentos);
                return;
            } catch (EnvioDefinitivoException e) {
                log.error("Error definitivo id={}: {}. NACK → DLQ", notificacion.id(), e.getMessage());
                channel.basicNack(tag, false, false);
                return;
            } catch (EnvioTemporalException e) {
                log.warn("Error temporal id={} (intento {}/{}): {}",
                        notificacion.id(), intento, maxIntentos, e.getMessage());
                if (intento < maxIntentos && !esperar(props.reintentos().esperaMs() * intento)) {
                    break;
                }
            } catch (RuntimeException e) {
                log.error("Error inesperado id={}. NACK → DLQ", notificacion.id(), e);
                channel.basicNack(tag, false, false);
                return;
            }
        }

        log.error("Reintentos agotados id={} ({} intentos). NACK → DLQ", notificacion.id(), maxIntentos);
        channel.basicNack(tag, false, false);
    }

    /** Espera entre reintentos. Devuelve false si el hilo fue interrumpido. */
    private boolean esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}