package cl.kioskonube.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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

    // ---- Envío de mensajes en JSON ----

    /**
     * Solo el RabbitTemplate (productor) usa JSON. Los consumidores reciben el Message crudo
     * y lo convierten con su propio JsonMapper, así aceptan mensajes de cualquier microservicio
     * sin depender del header __TypeId__ ni de clases de otros paquetes.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new JacksonJsonMessageConverter());
        return template;
    }
}