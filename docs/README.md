# Documentación – pqr-service

Servicio REST en Quarkus con un pipeline de Integración Continua (CI) en GitHub Actions.
Taller CI/CD – Maestría en Arquitectura de Software, Universidad de La Sabana.

## Orden de lectura

| # | Documento | Contenido |
|---|---|---|
| 1 | [01-entorno-local.md](01-entorno-local.md) | Herramientas, instalación y ejecución local |
| 2 | [02-pipeline-ci.md](02-pipeline-ci.md) | Stages del pipeline y construcción paso a paso, con evidencias |
| 3 | [03-problemas-y-soluciones.md](03-problemas-y-soluciones.md) | Inconvenientes encontrados en cada paso y su solución |
| 4 | [04-seleccion-herramientas.md](04-seleccion-herramientas.md) | Justificación de las herramientas según el contexto |

## Otros recursos

| Recurso | Ubicación |
|---|---|
| Diagrama de arquitectura (draw.io) | [arquitectura/arquitectura-cicd-gcp.drawio](arquitectura/arquitectura-cicd-gcp.drawio) |
| Evidencias (capturas) | [evidencias/](evidencias/) |
| Pipeline de CI | [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) |
| Análisis de seguridad (CodeQL) | [`.github/workflows/codeql.yml`](../.github/workflows/codeql.yml) |
| Actualizaciones automáticas | [`.github/dependabot.yml`](../.github/dependabot.yml) |
| Excepciones de vulnerabilidades | [`.trivyignore`](../.trivyignore) |

## Convención

Los pasos del pipeline se numeran del **0 al 10** y el número es el mismo en todos los documentos:
"Paso 6" en `02-pipeline-ci.md` corresponde a "Paso 6" en `03-problemas-y-soluciones.md`.
