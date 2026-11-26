# pqr-service

[![CI](https://github.com/inggabrielr/pqr-service/actions/workflows/ci.yml/badge.svg)](https://github.com/inggabrielr/pqr-service/actions/workflows/ci.yml)
[![CodeQL](https://github.com/inggabrielr/pqr-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/inggabrielr/pqr-service/actions/workflows/codeql.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=inggabrielr_pqr-service&metric=alert_status)](https://sonarcloud.io/project/overview?id=inggabrielr_pqr-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=inggabrielr_pqr-service&metric=coverage)](https://sonarcloud.io/project/overview?id=inggabrielr_pqr-service)

Servicio REST en Quarkus que recibe el id de una PQR y devuelve un mensaje de respuesta,
con un pipeline de Integración Continua (CI) en GitHub Actions.

Taller CI/CD – Maestría en Arquitectura de Software, Universidad de La Sabana.

## Pipeline de CI

En cada pull request hacia `main` y en cada merge:

| Etapa | Herramienta |
|---|---|
| Detección de secretos | Gitleaks + Secret scanning |
| Compilación, pruebas y cobertura | Maven + JUnit + JaCoCo |
| Dependencias vulnerables | Trivy + Dependabot |
| Calidad de código | SonarQube Cloud (OIDC, sin secretos) |
| Seguridad del código (SAST) | CodeQL |
| Imagen de contenedor | Jib |
| Escaneo de la imagen | Trivy |
| Rendimiento | k6 |
| Protección de `main` | Rulesets de GitHub (merge bloqueado si un check falla) |

## Ejecución local

Requisitos: Java 25 y Maven (ver [entorno local](docs/01-entorno-local.md)).

```bash
./mvnw quarkus:dev                          # puerto 9090
curl http://localhost:9090/pqr/PQR-2026
./mvnw clean verify                         # pruebas y cobertura
```

## Enlaces

| Recurso | Enlace |
|---|---|
| Documentación | [docs/](docs/README.md) |
| Ejecuciones del CI | [GitHub Actions](https://github.com/inggabrielr/pqr-service/actions) |
| Calidad y cobertura | [SonarQube Cloud](https://sonarcloud.io/project/overview?id=inggabrielr_pqr-service) |
| Alertas de seguridad | [Security](https://github.com/inggabrielr/pqr-service/security) |
| Diagrama de arquitectura | [docs/arquitectura/](docs/arquitectura/arquitectura-cicd-gcp.drawio) |

## Estructura

```
.github/workflows/   ci.yml (pipeline), codeql.yml (SAST)
.github/dependabot.yml
docs/                documentación, arquitectura y evidencias
k6/                  prueba de rendimiento
src/                 código y pruebas
.trivyignore         excepciones de vulnerabilidades documentadas
```
