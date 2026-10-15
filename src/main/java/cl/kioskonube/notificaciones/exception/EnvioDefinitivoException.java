package cl.kioskonube.notificaciones.exception;

/**
 * Error que NO se resuelve reintentando (ej: datos inválidos, credenciales malas).
 */
public class EnvioDefinitivoException extends RuntimeException {

    public EnvioDefinitivoException(String message) {
        super(message);
    }

    public EnvioDefinitivoException(String message, Throwable cause) {
        super(message, cause);
    }
}