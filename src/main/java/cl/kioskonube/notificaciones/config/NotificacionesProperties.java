package cl.kioskonube.notificaciones.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Parámetros de envío de notificaciones (modo, remitente y reintentos).
 */
@Validated
@ConfigurationProperties(prefix = "kioskonube.notificaciones")
public record NotificacionesProperties(
        @NotBlank String modoEnvio,
        @NotBlank String remitente,
        @NotNull @Valid Reintentos reintentos
) {

    public record Reintentos(
            @Min(1) int maxIntentos,
            @Min(0) long esperaMs
    ) {
    }

    public boolean esModoSmtp() {
        return "smtp".equalsIgnoreCase(modoEnvio);
    }
}