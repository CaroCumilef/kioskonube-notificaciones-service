package cl.kioskonube.notificaciones.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Nombres de exchanges, colas y routing keys del dominio de notificaciones.
 * Se leen desde application.yml (kioskonube.mensajeria.notificaciones).
 */
@Validated
@ConfigurationProperties(prefix = "kioskonube.mensajeria.notificaciones")
public record MensajeriaProperties(
        @NotBlank String exchange,
        @NotBlank String queue,
        @NotBlank String routingKey,
        @NotBlank String dlx,
        @NotBlank String dlq,
        @NotBlank String dlqRoutingKey
) {
}