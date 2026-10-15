package cl.kioskonube.notificaciones.exception;

/**
 * Error que puede resolverse reintentando (ej: servidor SMTP no disponible).
 */
public class EnvioTemporalException extends RuntimeException {

    public EnvioTemporalException(String message) {
        super(message);
    }

    public EnvioTemporalException(String message, Throwable cause) {
        super(message, cause);
    }
}