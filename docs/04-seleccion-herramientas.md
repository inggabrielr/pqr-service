# 04 – Selección de herramientas

Justificación de cada herramienta del pipeline de CI **según el contexto del proyecto**,
con la alternativa considerada y la contrapartida asumida.

## Contexto que guía la selección

| Factor | Situación | Implicación |
|---|---|---|
| Repositorio | GitHub (público) | Priorizar integración nativa con pull requests; capas gratuitas disponibles |
| Destino | Google Cloud (Artifact Registry, Cloud Run / GKE) | Imágenes OCI estándar; autenticación con Workload Identity Federation |
| Stack | Java 25, Quarkus, Maven | Herramientas del ecosistema Java; sin agregar lenguajes ni build tools nuevos |
| Sector de referencia | Financiero (regulado) | Seguridad en cada etapa, trazabilidad, mínimo privilegio, cero credenciales de larga duración |
| Equipo | Pequeño, sin equipo de plataforma | Preferir servicios administrados frente a infraestructura propia |

## Criterios aplicados

1. **Integración con el flujo del PR:** el resultado debe verse donde el equipo colabora.
2. **Operación mínima:** sin servidores que instalar, actualizar o escalar.
3. **Seguridad por diseño:** sin secretos de larga duración; binarios verificados; acciones de terceros fijadas por SHA.
4. **Estándares abiertos:** reducir la dependencia de proveedor (OCI, OIDC, JUnit, Kubernetes).
5. **Costo:** capa gratuita suficiente para el proyecto.

## Herramientas

| Herramienta | Función | Alternativas consideradas | Por qué en este contexto | Contrapartida |
|---|---|---|---|---|
| **GitHub** | Repositorio, PR, revisión | GitLab, Cloud Source Repositories | Ya aloja el código; Cloud Source Repositories no está disponible para cuentas nuevas | Dependencia media (PR y reglas propias de GitHub) |
| **GitHub Actions** | Orquestación del CI | Jenkins, GitLab CI, Cloud Build | Pipeline junto al código y resultado en el PR; sin servidor; runners bajo demanda; curva de aprendizaje corta | Sintaxis propia; minutos limitados en repos privados |
| **Maven Wrapper + SDKMAN** | Versiones de build y JDK | Maven global, Gradle | Misma versión en local y en CI; Maven es el build nativo de Quarkus | Ninguna relevante |
| **JUnit 5 + RestAssured + `@QuarkusTest`** | Pruebas unitarias y de API | Spring Test, Postman/Newman | Estándar Java; prueba la API real dentro del build | Pruebas de API más lentas que las unitarias |
| **JaCoCo** (`quarkus-jacoco` + plugin Maven) | Cobertura | Cobertura del IDE, OpenClover | Estándar abierto que lee SonarQube; combina pruebas unitarias y de Quarkus | Configuración más elaborada para combinar ambas fuentes |
| **SonarQube Cloud** | Calidad y *quality gate* | SonarQube autoalojado, Codacy | Sin servidor; gratis para repos públicos; análisis sin secretos (OIDC) | Dependencia baja: las reglas son las de SonarQube autoalojable |
| **Gitleaks + Secret scanning** | Secretos | TruffleHog, solo Secret scanning | Secret scanning bloquea el push (proveedores conocidos); Gitleaks cubre patrones genéricos y todo el historial en el CI | Posibles falsos positivos (se gestionan con `.gitleaksignore`) |
| **CodeQL** | SAST | Semgrep, solo SonarQube | Nativo de GitHub, especializado en flujo de datos; gratis en repos públicos | En repos privados requiere licencia |
| **Dependabot** | Actualización de dependencias y acciones | Renovate | Nativo de GitHub, sin instalación; cubre Maven y GitHub Actions | No actualiza versiones definidas en variables del `ci.yml` |
| **Trivy** | Vulnerabilidades en dependencias e imagen | OWASP Dependency-Check, Grype, Artifact Analysis | Una herramienta para dependencias e imagen; bloquea **antes** de publicar (Artifact Analysis actúa después) | Requiere `--offline-scan` para no depender de Maven Central |
| **Jib** (extensión de Quarkus) | Imagen de contenedor | Dockerfile + Docker, Buildpacks | Sin Dockerfile ni demonio Docker para construir; capas separadas; builds reproducibles | Menos control fino que un Dockerfile |
| **UBI 9 `openjdk-25-runtime`** | Imagen base | Eclipse Temurin, distroless | Imagen soportada por Red Hat y usada por defecto por Quarkus; versión fijada y escaneada | Correcciones del sistema base dependen de Red Hat (ver `.trivyignore`) |
| **k6** | Rendimiento | JMeter, Gatling | Scripts como código (JavaScript), umbrales que fallan el CI, binario liviano | En CI es solo prueba de humo; la carga real va en qa |
| **Rulesets de GitHub** | Protección de `main` | Reglas clásicas de protección de rama | Mecanismo actual de GitHub; hace obligatorios los checks | Con un solo desarrollador, 0 aprobaciones requeridas |

## Decisiones transversales

| Decisión | Justificación |
|---|---|
| **OIDC en lugar de tokens guardados** (SonarQube Cloud; GCP con Workload Identity Federation) | Credenciales temporales; nada que robar ni rotar |
| **Binarios oficiales verificados** (Gitleaks, Trivy, k6) en lugar de acciones de terceros | Menos dependencias de terceros; descarga solo HTTPS y validada con *checksum*. Contrapartida: versiones actualizadas manualmente |
| **Acciones de terceros fijadas por SHA** | Evita ataques a la cadena de suministro por etiquetas movidas |
| **Permisos mínimos por workflow** (`contents: read`, `id-token: write`; CodeQL aparte) | Cada pipeline tiene solo lo que necesita |
| **Excepciones con vencimiento** (`.trivyignore` con `exp:`) | El riesgo aceptado se documenta y se revisa obligatoriamente |

## Costo

Todas las herramientas del CI son gratuitas para este proyecto (repositorio público y capas gratuitas).
En un repositorio privado tendrían costo CodeQL (GitHub Code Security), Secret scanning y los minutos de GitHub Actions
por encima de la cuota del plan.
