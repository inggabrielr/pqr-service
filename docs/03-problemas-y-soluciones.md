# 03 – Problemas y soluciones

Inconvenientes encontrados al construir el pipeline, organizados por paso
(misma numeración de [02-pipeline-ci.md](02-pipeline-ci.md)).
Formato: **síntoma** (mensaje real) → **causa** → **solución**.
En los pasos sin inconvenientes se deja un tip.

---

## Entorno local y Git

### E1. `mvn` no encontrado
- **Síntoma:** `zsh: command not found: mvn`
- **Causa:** Maven no estaba instalado.
- **Solución:** instalarlo con SDKMAN (ver [01-entorno-local.md](01-entorno-local.md)).

### E2. Java existente no reconocido por macOS
- **Síntoma:** `sdk install java 24-local $(/usr/libexec/java_home -v 24)` → `Unable to locate a Java Runtime`.
- **Causa:** el Java 24 ya lo administraba SDKMAN (`~/.sdkman/candidates/java/24.0.2-tem`), no estaba en la carpeta estándar de macOS.
- **Solución:** no registrar nada; instalar Java 25 con SDKMAN.
  ```bash
  readlink -f $(which java)   # muestra quién administra el Java activo
  ```

### E3. Descarga de Maven interrumpida
- **Síntoma:** `curl: (35) Recv failure: Connection reset by peer`
- **Causa:** corte de red durante la descarga.
- **Solución:**
  ```bash
  sdk flush tmp
  sdk install maven
  ```

### E4. CLI de Quarkus no encontrado
- **Síntoma:** `zsh: command not found: quarkus`
- **Solución:** usar el plugin de Maven (no requiere el CLI):
  ```bash
  ./mvnw quarkus:add-extension -Dextensions="container-image-jib"
  ```

### E5. Puerto 8080 ocupado
- **Síntoma:** `Port 8080 seems to be in use by another process`
- **Solución:** puerto 9090 solo para desarrollo en `application-dev.properties`; `lsof -i :8080` para identificar el proceso.

### E6. `Repository not found` al hacer push
- **Causa:** clonar no crea el repositorio en la cuenta propia; el remoto apuntaba a un repo inexistente.
- **Solución:** crear el repo antes del push (o usar *fork*):
  ```bash
  gh auth login
  gh repo create pqr-service --public --source=. --remote=origin --push
  ```

### E7. `gh release list` con "error connecting to github"
- **Causa:** `-R` recibió `github/codeql-action/init`; solo acepta `dueño/repositorio`.
- **Solución:** `gh release list -R github/codeql-action --limit 5`

---

## Paso 0. Base del pipeline
- **Tip:** fijar versiones (Maven Wrapper y `.sdkmanrc`) elimina diferencias entre el entorno local y el CI.

## Paso 1. Compilación y pruebas

### 1.1 Acciones con Node.js 20 deprecado
- **Síntoma:** `Node.js 20 is deprecated... actions/checkout@v4, actions/setup-java@v4...`
- **Causa:** versiones antiguas de las acciones.
- **Solución:** actualizar la versión mayor; consultar la vigente:
  ```bash
  gh release list -R actions/cache --limit 5
  gh api "repos/actions/cache/contents/action.yml?ref=v6" --jq .content | base64 -d | grep using   # debe ser node24
  ```
  Prevención: Dependabot con `package-ecosystem: github-actions`.

### 1.2 Prueba de API falla al ejecutar solo el método en el IDE
- **Causa probable:** puerto de pruebas 8081 ocupado (modo dev con pruebas continuas) o compilación desactualizada.
- **Solución:**
  ```bash
  mvn clean test-compile
  ./mvnw test -Dtest=PqrResourceTest#consultaPqrValida
  ```

## Paso 2. Cobertura con JaCoCo

### 2.1 Las pruebas unitarias no sumaban cobertura
- **Causa:** `quarkus-jacoco` solo instrumenta clases cargadas por Quarkus (`@QuarkusTest`).
- **Solución:** `jacoco-maven-plugin` con `prepare-agent`, `exclClassLoaders=*QuarkusClassLoader` y el mismo `jacoco-quarkus.exec`.

### 2.2 Reporte no generado sin pruebas de Quarkus
- **Síntoma:** `target/jacoco-report/index.html does not exist`
- **Causa:** el reporte lo generaba `quarkus-jacoco` al terminar un `@QuarkusTest`.
- **Solución:** meta `report` del plugin en la fase `verify` + `quarkus.jacoco.report=false`.

### 2.3 Riesgo de perder datos de las pruebas unitarias
- **Causa:** Quarkus borra `jacoco-quarkus.exec` al arrancar; si las unitarias corren antes, sus datos se pierden (depende del orden).
- **Solución:** `quarkus.jacoco.reuse-data-file=true`.

### 2.4 Advertencias de JaCoCo en la imagen de producción
- **Síntoma:** `Unrecognized configuration key "quarkus.jacoco.reuse-data-file"`
- **Causa:** propiedades de una extensión de pruebas en la configuración general.
- **Solución:** moverlas a `application-test.properties`.

## Paso 3. Calidad con SonarQube Cloud

### 3.1 Análisis automático en lugar del CI
- **Causa:** al importar el proyecto, SonarQube Cloud lo analiza por su cuenta (sin cobertura y sin bloquear el CI).
- **Solución:** proyecto → **Administration → Analysis Method** → desactivar **Automatic Analysis**.

### 3.2 Token de larga duración
- **Decisión:** usar **secretless analysis** (OIDC) en lugar de `SONAR_TOKEN` en secretos: token de 30 min, sin rotación.
- **Solución:** activar en la organización + `id-token: write` + `SonarSource/sonarqube-auth-action`.

### 3.3 *Quality gate* fallido por inyección por campo
- **Síntoma:** `QUALITY GATE STATUS: FAILED` – regla `java:S6813` "Remove this field injection and use constructor injection instead."
- **Causa:** primer análisis sin línea base: todo el código se evaluó como nuevo.
- **Solución:** inyección por constructor en `PqrResource` (campo `final`, sin `@Inject`).

### 3.4 Acción de terceros sin fijar
- **Síntoma:** "Use full commit SHA hash for this dependency."
- **Causa:** `@v1` es una etiqueta movible (riesgo de cadena de suministro).
- **Solución:**
  ```bash
  gh release list -R SonarSource/sonarqube-auth-action --limit 5
  gh api repos/SonarSource/sonarqube-auth-action/commits/<versión> --jq .sha
  ```
  y usar `uses: SonarSource/sonarqube-auth-action@<SHA>`.

## Paso 4. Detección de secretos

### 4.1 *Hotspot* en la descarga con `curl`
- **Síntoma:** "Not enforcing HTTPS here might allow for redirections to insecure websites."
- **Causa:** `curl -L` sigue redirecciones sin restringir el protocolo.
- **Solución:** `--proto '=https' --tlsv1.2`, descargar a disco (`-O`) y validar con `sha256sum --check` antes de ejecutar.

### 4.2 Secreto de ejemplo en la documentación
- **Síntoma:** `leaks found: 1` – `RuleID: generic-api-key` en `docs/02-pipeline-ci.md`.
- **Causa:** el valor de ejemplo de la prueba controlada estaba escrito en el Markdown; Gitleaks revisa todos los archivos y todo el historial.
- **Solución:** reemplazar el valor por un marcador (`<valor>`) y rehacer el commit de la rama no integrada:
  ```bash
  git commit --amend --no-edit
  git push --force-with-lease
  ```
- **Lección:** la documentación también puede filtrar secretos; usar marcadores, nunca valores con forma de credencial.

- **Tip:** push protection solo bloquea patrones de proveedores conocidos; los genéricos los detecta Gitleaks.

## Paso 5. CodeQL
- **Tip:** desactivar la configuración "por defecto" de CodeQL antes de usar la avanzada; ambas activas entran en conflicto.
- **Tip:** en `gh release list -R github/codeql-action`, `codeql-bundle-*` es el motor; la versión de la acción es `vN.x.x`.

## Paso 6. Dependencias de terceros

### 6.1 Trivy bloqueado por Maven Central
- **Síntoma:** `remote Maven repository returned 429 Too Many Requests ... Retry-After: 1800`
- **Causa:** Trivy descargaba POM desde Maven Central usando las IP compartidas de los runners.
- **Solución:** ejecutar Trivy **después** de compilar y con `--offline-scan` (usa `~/.m2`).

### 6.2 Versión inexistente
- **Síntoma:** `curl: (22) The requested URL returned error: 404`
- **Causa:** versión de ejemplo en `TRIVY_VERSION`.
- **Solución:**
  ```bash
  gh release list -R aquasecurity/trivy --limit 3
  gh release view v<versión> -R aquasecurity/trivy --json assets --jq '.assets[].name' | grep -iE "linux-64bit.tar.gz|checksums"
  ```

- **Tip:** los PR de Dependabot no requieren acción inmediata; si no se integran, `main` no cambia.

## Paso 7. Imagen con Jib

### 7.1 Sin motor de contenedores local
- **Síntoma:** `Cannot get an executable name when no container runtime is available`
- **Solución:** `brew install colima docker && colima start` (en el CI, Docker ya existe).

### 7.2 Contenedor sin respuesta
- **Síntoma:** `curl: (52) Empty reply from server`; log: `Listening on: http://0.0.0.0:9090`
- **Causa:** `quarkus.http.port=9090` aplicaba a todos los perfiles; `docker run -p 9090:8080` apuntaba al 8080 del contenedor.
- **Solución:** configuración por perfiles (`application-dev.properties` con 9090; 8080 por defecto en los demás).

## Paso 8. Escaneo de la imagen con Trivy

### 8.1 Vulnerabilidades en la imagen base
- **Síntoma:** 9 vulnerabilidades HIGH en `curl-minimal`, `libcurl-minimal` y `expat` (UBI 9.9).
- **Causa:** Red Hat publicó los paquetes corregidos, pero aún no la imagen de Java reconstruida.
- **Análisis:**

  | Imagen base | HIGH | CRITICAL |
  |---|---|---|
  | `1.24` (etiqueta flotante, usada por defecto) | 9 | 0 |
  | `1.24-5` (más reciente) | 9 | 0 |
  | `1.24-3.1794341158` (reconstrucción anterior) | 44 | 2 (incluida RCE en OpenJDK) |

- **Solución:**
  1. Fijar `quarkus.jib.base-jvm-image=...openjdk-25-runtime:1.24-5` (build reproducible).
  2. Excepción temporal en `.trivyignore`, justificada y con vencimiento:
     ```
     CVE-2026-8286 exp:2026-12-31
     CVE-2026-8458 exp:2026-12-31
     CVE-2026-8927 exp:2026-12-31
     CVE-2026-9547 exp:2026-12-31
     CVE-2026-115506 exp:2026-12-31
     ```
     Riesgo bajo: la aplicación no invoca `curl`, y Java no usa `expat`. Al vencer, el CI vuelve a fallar.

- **Tip:** las etiquetas con marca de tiempo (`1.24-3.<n>`) no son necesariamente las más recientes; comparar escaneando.

## Paso 9. Rendimiento con k6

### 9.1 Versión de ejemplo
- **Síntoma:** `curl: (22) ... 404` con `K6_VERSION: 1.x.x`.
- **Solución:** `gh release list -R grafana/k6 --limit 3`.

### 9.2 Conflicto de nombres al extraer
- **Síntoma:** `tar: k6: Cannot open: File exists`
- **Causa:** el binario `k6` chocaba con la carpeta `k6/` del proyecto.
- **Solución:** descargar y extraer en `$RUNNER_TEMP` y ejecutar `"$RUNNER_TEMP/k6"`.

## Paso 10. Protección de la rama `main`

### 10.1 Merge de un PR equivocado
- **Síntoma:** `gh pr merge 11` integró un PR de Dependabot en lugar del previsto.
- **Causa:** varios PR abiertos; selección por número sin revisar el título.
- **Solución:** verificar el commit en `main` y su CI; revertir solo si es necesario (sin reescribir historia):
  ```bash
  git log --oneline -5
  gh run list --branch main --limit 1
  git revert <sha>        # solo si hay que deshacer
  ```
  Prevención: protección de rama (no permite merge con checks en rojo).

### 10.2 Aprobaciones con un solo desarrollador
- **Causa:** GitHub no permite aprobar el propio PR.
- **Solución:** *Required approvals* = 0 (en equipo, al menos 1).

### 10.3 `gh pr close` sin argumentos
- **Síntoma:** `cannot close pull request: number, url, or branch required`
- **Solución:** `gh pr close test/proteccion-rama --delete-branch`

### 10.4 Probar el bloqueo sin dejar código roto
- **Solución:** interruptor `taller.demo.fallo-ci` (ver [02 – Paso 10](02-pipeline-ci.md#paso-10-protección-de-la-rama-main)).
