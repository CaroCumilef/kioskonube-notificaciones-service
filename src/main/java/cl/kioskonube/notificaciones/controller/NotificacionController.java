package cl.kioskonube.notificaciones.controller;

import cl.kioskonube.notificaciones.dto.NotificacionEncoladaResponse;
import cl.kioskonube.notificaciones.dto.NotificacionRequest;
import cl.kioskonube.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificacionEncoladaResponse> crear(@Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.encolar(request));
    }
}