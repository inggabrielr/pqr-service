# pqr-service

Servicio REST en Quarkus que recibe el id de una PQR y devuelve, de forma aleatoria,
uno de 10 mensajes de respuesta. Taller CI/CD (GitHub → Google Cloud → GKE).

## Requisitos

- Java 25
- Maven 3.9+

## Ejecutar en modo desarrollo

```bash
mvn quarkus:dev
```

## Probar el servicio

```bash
curl http://localhost:8080/pqr/PQR-2026
```

Respuesta de ejemplo:

```json
{"id":"PQR-2026","mensaje":"La PQR PQR-2026 fue asignada a un asesor especializado."}
```

El id solo admite letras, números y guiones (máximo 20 caracteres); otro valor devuelve `400 Bad Request`.

Salud del servicio (para las sondas de Kubernetes):

```bash
curl http://localhost:8080/q/health/ready
```

## Pruebas

```bash
mvn test
```

## Empaquetar

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```
