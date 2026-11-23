// ============================================================
// Prueba de rendimiento ligera (smoke test) – pqr-service
// Simula 10 usuarios consultando PQR durante 30 segundos y
// verifica que el servicio responda bien y rápido.
// Si no se cumplen los umbrales, k6 termina con error y el CI falla.
// ============================================================
import http from 'k6/http';
import { check, sleep } from 'k6';

// Dirección del servicio; en el CI apunta al contenedor levantado.
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  // 10 usuarios virtuales simultáneos durante 30 segundos.
  vus: 10,
  duration: '30s',

  // Criterios de aceptación: si alguno no se cumple, la prueba falla.
  thresholds: {
    // Menos del 1% de las peticiones pueden fallar.
    http_req_failed: ['rate<0.01'],
    // El 95% de las respuestas debe tardar menos de 500 ms.
    http_req_duration: ['p(95)<500'],
    // Más del 99% de las validaciones (checks) deben cumplirse.
    checks: ['rate>0.99'],
  },
};

export default function () {
  // Id distinto por usuario e iteración, válido para el servicio
  // (solo letras, números y guiones, máximo 20 caracteres).
  const id = `PQR-${__VU}-${__ITER % 1000}`;

  const respuesta = http.get(`${BASE_URL}/pqr/${id}`);

  // Valida el contenido de la respuesta, no solo que responda.
  check(respuesta, {
    'responde 200': (r) => r.status === 200,
    'devuelve el id consultado': (r) => r.json('id') === id,
    'el mensaje contiene el id': (r) => String(r.json('mensaje')).includes(id),
  });

  // Pausa breve entre peticiones, como haría un usuario real.
  sleep(0.5);
}