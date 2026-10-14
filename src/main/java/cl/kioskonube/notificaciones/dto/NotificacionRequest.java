package cl.kioskonube.notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos que recibe POST /notificaciones.
 */
public record NotificacionRequest(
        @NotBlank(message = "El destinatario es obligatorio")
        @Email(message = "El destinatario debe ser un correo válido")
        String destinatario,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
        String asunto,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 5000, message = "El mensaje no puede superar 5000 caracteres")
        String mensaje,

        @Size(max = 50, message = "El origen no puede superar 50 caracteres")
        String origen
) {
}