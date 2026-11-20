package com.taller.pqr;

/**
 * Respuesta de la consulta de una PQR.
 *
 * @param id      identificador de la PQR consultada
 * @param mensaje mensaje de respuesta asignado aleatoriamente
 */
public record PqrResponse(String id, String mensaje) {
}
