package cl.kioskonube.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declaración de la topología RabbitMQ del dominio de notificaciones.
 * Solo configuración: aquí no hay lógica de negocio.
 */
@Configuration
public class RabbitConfig {

    private final MensajeriaProperties props;

    public RabbitConfig(MensajeriaProperties props) {
        this.props = props;
    }

    // ---- Flujo principal ----

    @Bean
    public DirectExchange notificacionesExchange() {
        return new DirectExchange(props.exchange(), true, false);
    }

    @Bean
    public Queue notificacionesQueue() {
        return QueueBuilder.durable(props.queue())
                .deadLetterExchange(props.dlx())
                .deadLetterRoutingKey(props.dlqRoutingKey())
                .build();
    }

    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue())
                .to(notificacionesExchange())
                .with(props.routingKey());
    }

    // ---- Dead Letter (mensajes que fallaron definitivamente) ----

    @Bean
    public DirectExchange notificacionesDlx() {
        return new DirectExchange(props.dlx(), true, false);
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(props.dlq()).build();
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq())
                .to(notificacionesDlx())
                .with(props.dlqRoutingKey());
    }

    // ---- Serialización JSON de los mensajes ----

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
