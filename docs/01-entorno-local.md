# 01 – Entorno local

Herramientas necesarias para desarrollar, probar y reproducir localmente lo que hace el pipeline.
Entorno de referencia: macOS con Homebrew.

## Herramientas

| Herramienta | Para qué | Instalación |
|---|---|---|
| SDKMAN | Administrar versiones de Java y Maven | `curl` (ver abajo) |
| Java 25 (Temurin) | Compilar y ejecutar el servicio | SDKMAN |
| Maven | Construcción del proyecto (el proyecto trae Maven Wrapper `./mvnw`) | SDKMAN |
| Quarkus CLI (opcional) | Crear proyectos y agregar extensiones | SDKMAN |
| GitHub CLI (`gh`) | Repositorios, PR y estado del CI desde la terminal | Homebrew |
| Colima + Docker CLI | Construir y ejecutar la imagen de contenedor | Homebrew |
| Trivy | Escanear dependencias e imágenes | Homebrew |
| Gitleaks | Buscar secretos en el código e historial | Homebrew |
| k6 | Pruebas de rendimiento | Homebrew |
| skopeo | Consultar etiquetas de imágenes en registros | Homebrew |

> Regla práctica: **SDKMAN** para el ecosistema Java (JDK, Maven, Quarkus CLI) y **Homebrew** para lo demás.
> No instalar la misma herramienta con ambos.

## 1. SDKMAN, Java y Maven

```bash
# Instalar SDKMAN (si ya existe, el instalador no hace cambios)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Ver versiones de Java disponibles (Temurin termina en -tem)
sdk list java | grep "25.*tem"

# Instalar Java 25 y Maven (responder Y para dejarlos por defecto)
sdk install java 25.0.5.1+2-tem
sdk install maven

# Verificar
java -version
mvn -v          # "Java version" debe ser 25
```

Cambiar de versión:

```bash
sdk current                       # versiones activas
sdk use java 24.0.2-tem           # solo en la terminal actual
sdk default java 25.0.5.1+2-tem   # por defecto en todas las terminales
sdk env                           # aplica las versiones del .sdkmanrc del proyecto
```

> Si `~/.zshrc` tiene un `export JAVA_HOME=...`, eliminarlo: SDKMAN administra `JAVA_HOME`.
> Las líneas de SDKMAN deben quedar al final del archivo.

## 2. Herramientas de Homebrew

```bash
brew install gh colima docker trivy gitleaks k6 skopeo
```

Autenticar GitHub CLI (elegir GitHub.com → HTTPS → Yes → Login with a web browser):

```bash
gh auth login
gh auth status
```

Arrancar el motor de contenedores:

```bash
colima start
docker ps
```

## 3. Ejecutar el servicio

```bash
./mvnw quarkus:dev                         # modo desarrollo, puerto 9090
curl http://localhost:9090/pqr/PQR-2026    # consulta de una PQR
curl http://localhost:9090/q/health/ready  # salud del servicio
```

Teclas en modo dev: `r` ejecuta pruebas, `d` abre la Dev UI, `q` detiene.

## 4. Pruebas y cobertura

```bash
./mvnw clean verify                   # compila, prueba y genera cobertura
open target/jacoco-report/index.html  # reporte de cobertura
```

## 5. Imagen de contenedor

```bash
./mvnw package -DskipTests -Dquarkus.container-image.build=true -Dquarkus.container-image.tag=local
docker run --rm -p 9090:8080 taller/pqr-service:local
curl http://localhost:9090/pqr/PQR-2026
```

## 6. Reproducir los controles del pipeline

```bash
gitleaks git . --redact --verbose                                                 # secretos
trivy fs . --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed             # dependencias
trivy image taller/pqr-service:local --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed  # imagen
BASE_URL=http://localhost:9090 k6 run k6/smoke.js                                 # rendimiento
```

## Configuración por perfiles

| Archivo | Perfil | Contenido |
|---|---|---|
| `application.properties` | Todos | Nombre de la app, imagen (Jib) e imagen base |
| `application-dev.properties` | `dev` (`quarkus:dev`) | Puerto 9090 |
| `application-test.properties` | `test` (pruebas) | Configuración de JaCoCo e interruptor de demostración |

En contenedores y Kubernetes el servicio usa el puerto **8080** (valor por defecto).
