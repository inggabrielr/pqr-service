package com.taller.pqr;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.regex.Pattern;

/**
 * API REST de consulta de PQR.
 *
 * GET /pqr/{id} -> {"id": "...", "mensaje": "..."}
 */
@Path("/pqr")
@Produces(MediaType.APPLICATION_JSON)
public class PqrResource {

    /** Solo letras, números y guiones, de 1 a 20 caracteres. */
    private static final Pattern ID_VALIDO = Pattern.compile("^[A-Za-z0-9-]{1,20}$");

    private final PqrService service;

    /** Quarkus inyecta el servicio a través del constructor. */
    public PqrResource(PqrService service) {
        this.service = service;
    }

    @GET
    @Path("/{id}")
    public PqrResponse consultar(@PathParam("id") String id) {
        if (!ID_VALIDO.matcher(id).matches()) {
            throw new BadRequestException(
                    "El id de la PQR solo admite letras, números y guiones (máximo 20 caracteres).");
        }
        return service.consultar(id);
    }
}
