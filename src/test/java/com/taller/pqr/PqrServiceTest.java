package com.taller.pqr;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas unitarias de la lógica, sin levantar Quarkus. */
class PqrServiceTest {

    private final PqrService service = new PqrService(new Random(42));

    @Test
    void devuelveElIdConsultado() {
        assertEquals("PQR-001", service.consultar("PQR-001").id());
    }

    @Test
    void elMensajeContieneElIdYPerteneceAlCatalogo() {
        PqrResponse respuesta = service.consultar("123");

        assertTrue(respuesta.mensaje().contains("123"));
        assertTrue(PqrService.MENSAJES.stream()
                .anyMatch(m -> m.formatted("123").equals(respuesta.mensaje())));
    }

    @Test
    void entregaVariosMensajesDistintos() {
        Set<String> distintos = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            distintos.add(service.consultar("1").mensaje());
        }
        assertEquals(PqrService.MENSAJES.size(), distintos.size(),
                "En 200 consultas deberían aparecer los 10 mensajes");
    }
}
