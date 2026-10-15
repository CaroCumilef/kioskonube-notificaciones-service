package cl.kioskonube.notificaciones.service;

import cl.kioskonube.notificaciones.config.NotificacionesProperties;
import cl.kioskonube.notificaciones.exception.EnvioDefinitivoException;
import cl.kioskonube.notificaciones.exception.EnvioTemporalException;
import cl.kioskonube.notificaciones.messaging.NotificacionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio del envío de correos. No conoce RabbitMQ.
 * Clasifica los errores en temporales (reintentables) o definitivos.
 */
@Service
public class EnvioNotificacionService {

    private static final Logger log = LoggerFactory.getLogger(EnvioNotificacionService.class);

    // Dominios para forzar fallos en modo simulado (solo pruebas)
    private static final String DOMINIO_FALLA_TEMPORAL = "@falla-temporal.test";
    private static final String DOMINIO_FALLA_DEFINITIVA = "@falla-definitiva.test";

    private final JavaMailSender mailSender;
    private final NotificacionesProperties props;

    public EnvioNotificacionService(JavaMailSender mailSender, NotificacionesProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    public void enviar(NotificacionMessage notificacion) {
        validar(notificacion);
        if (props.esModoSmtp()) {
            enviarPorSmtp(notificacion);
        } else {
            simular(notificacion);
        }
    }

    private void validar(NotificacionMessage n) {
        if (n.destinatario() == null || !n.destinatario().contains("@")) {
            throw new EnvioDefinitivoException("Destinatario inválido: " + n.destinatario());
        }
        if (n.asunto() == null || n.asunto().isBlank()) {
            throw new EnvioDefinitivoException("La notificación no tiene asunto");
        }
    }

    private void simular(NotificacionMessage n) {
        String destino = n.destinatario().toLowerCase();
        if (destino.endsWith(DOMINIO_FALLA_TEMPORAL)) {
            throw new EnvioTemporalException("Fallo temporal simulado: servidor de correo no disponible");
        }
        if (destino.endsWith(DOMINIO_FALLA_DEFINITIVA)) {
            throw new EnvioDefinitivoException("Fallo definitivo simulado: buzón inexistente");
        }
        log.info("[SIMULADO] Correo enviado id={} para={} asunto=\"{}\"",
                n.id(), n.destinatario(), n.asunto());
    }

    private void enviarPorSmtp(NotificacionMessage n) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(props.remitente());
        mail.setTo(n.destinatario());
        mail.setSubject(n.asunto());
        mail.setText(n.mensaje());
        try {
            mailSender.send(mail);
            log.info("[SMTP] Correo enviado id={} para={}", n.id(), n.destinatario());
        } catch (MailAuthenticationException | MailParseException | MailPreparationException e) {
            // Credenciales o datos malos: reintentar no sirve
            throw new EnvioDefinitivoException("Error definitivo de correo: " + e.getMessage(), e);
        } catch (MailException e) {
            // Conexión, timeout, servidor caído: vale la pena reintentar
            throw new EnvioTemporalException("Error temporal de correo: " + e.getMessage(), e);
        }
    }
}