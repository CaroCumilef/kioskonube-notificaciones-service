package cl.kioskonube.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KioskonubeNotificacionesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KioskonubeNotificacionesServiceApplication.class, args);
    }
}