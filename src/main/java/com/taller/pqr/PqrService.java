package com.taller.pqr;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Lógica de negocio: devuelve uno de 10 mensajes posibles, elegido al azar,
 * para la PQR consultada.
 */
@ApplicationScoped
public class PqrService {

    /** Catálogo de respuestas. %s se reemplaza por el id de la PQR. */
    static final List<String> MENSAJES = List.of(
            "Su PQR %s fue radicada exitosamente y se encuentra en revisión.",
            "La PQR %s fue asignada a un asesor especializado.",
            "Su PQR %s está en análisis; recibirá respuesta en un plazo máximo de 15 días hábiles.",
            "La PQR %s requiere información adicional. Por favor revise su correo electrónico.",
            "Su PQR %s fue resuelta a su favor. Le enviaremos el detalle por correo.",
            "La PQR %s fue escalada al área de calidad para su seguimiento.",
            "Su PQR %s se encuentra en validación de documentos.",
            "La PQR %s fue cerrada. Gracias por ayudarnos a mejorar.",
            "Su PQR %s fue trasladada al área competente para su gestión.",
            "La PQR %s tiene una respuesta disponible en el portal de clientes."
    );

    private final RandomGenerator random;

    public PqrService() {
        this(new SecureRandom());
    }

    /** Constructor para pruebas: permite inyectar un generador con semilla fija. */
    PqrService(RandomGenerator random) {
        this.random = random;
    }

    public PqrResponse consultar(String id) {
        String plantilla = MENSAJES.get(random.nextInt(MENSAJES.size()));
        return new PqrResponse(id, plantilla.formatted(id));
    }
}
