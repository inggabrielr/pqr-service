package com.taller.pqr;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Prueba de DEMOSTRACIÓN del pipeline, no funcional.
 * Comprueba que la protección de la rama main bloquea el merge cuando el CI falla.
 *
 * Diseño a prueba de errores (fail-safe): si la propiedad queda en true por error,
 * el único efecto es bloquear el merge, nunca dejar pasar un cambio indebido.
 *
 * Uso: en una rama de prueba, poner taller.demo.fallo-ci=true en
 * application-test.properties, abrir el PR y verificar el bloqueo.
 * En main debe estar SIEMPRE en false.
 */
@QuarkusTest
class DemostracionProteccionRamaTest {

    private static final String PROPIEDAD = "taller.demo.fallo-ci";

    @ConfigProperty(name = PROPIEDAD, defaultValue = "false")
    boolean falloActivo;

    @Test
    void fallaSoloSiLaDemostracionEstaActiva() {
        // Con la propiedad en false, la prueba se omite (aparece como "skipped").
        assumeTrue(falloActivo, "Demostración de fallo del CI desactivada (" + PROPIEDAD + "=false)");

        fail("""

                ==================================================================
                 BLOQUEO INTENCIONAL DEL CI – NO ES UN ERROR DEL CÓDIGO
                ==================================================================
                 La propiedad '%s' está en true.
                 Esta prueba falla a propósito para demostrar que la protección
                 de la rama main impide hacer merge cuando el CI está en rojo.

                 Para poder hacer merge: cámbiela a false en
                 src/main/resources/application-test.properties
                ==================================================================
                """.formatted(PROPIEDAD));
    }
}