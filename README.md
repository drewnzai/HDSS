# HDSS

A **Health and Demographic Surveillance System (HDSS)** platform for collecting, managing, storing, and analysing demographic and health-related data about a population within a defined geographic area.

The platform consists of three main applications:

- **Web Frontend / Back Office** — React, Vite, and TypeScript
- **Backend API** — Java and Spring Boot
- **Android Application** — Kotlin and Jetpack Compose

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Clone the Repository](#clone-the-repository)
- [Docker Setup](#docker-setup) — recommended
  - [Before You Start: Required Files](#before-you-start-required-files)
  - [Docker Prerequisites](#docker-prerequisites)
  - [Docker Project Files](#docker-project-files)
  - [Environment Variables (.env)](#environment-variables-env)
  - [Database Dump](#database-dump)
  - [Android APK in Docker](#android-apk-in-docker)
  - [Running the Stack](#running-the-stack)
  - [Viewing Logs](#viewing-logs)
  - [Common Docker Commands](#common-docker-commands)
  - [Port Conflicts](#port-conflicts)
- [Observability (Prometheus & Grafana)](#observability-prometheus--grafana)
  - [Backend Dependencies](#backend-dependencies)
  - [Backend Configuration](#backend-configuration)
  - [Security Configuration](#security-configuration)
  - [Prometheus Configuration](#prometheus-configuration)
  - [Grafana Configuration](#grafana-configuration)
  - [Accessing Prometheus and Grafana](#accessing-prometheus-and-grafana)
  - [Verifying the Setup](#verifying-the-setup)
  - [Importing a Dashboard](#importing-a-dashboard)
- [Manual Setup (Without Docker)](#manual-setup-without-docker)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Android Setup](#android-setup)
- [Configuration](#configuration)
- [Database](#database)
- [Redis](#redis)
- [API Documentation](#api-documentation)
- [Authentication and Security](#authentication-and-security)
- [CORS Configuration](#cors-configuration)
- [Mail Configuration](#mail-configuration)
- [Development](#development)
- [Building for Production](#building-for-production)
- [Contributing](#contributing)
- [Security Considerations](#security-considerations)
- [License](#license)

## Features

HDSS provides a foundation for:

- Population and household data management
- Demographic data collection and management
- Health-related data management
- Secure authentication and authorization
- JWT-based access and refresh tokens
- RESTful API communication
- Data storage and retrieval
- Population-level data analysis
- Data caching using Redis
- Email-based functionality
- Cross-Origin Resource Sharing (CORS) configuration
- API documentation through Swagger / OpenAPI
- Metrics collection and visualization through Prometheus and Grafana
- Android-based field data collection
- Web-based administrative and back-office functionality

> The exact features available may depend on the modules and functionality implemented in the current version of the project.

## Architecture

```text
                         ┌─────────────────────┐
                         │    Android App      │
                         │ Kotlin + Compose    │
                         └──────────┬──────────┘
                                    │
                                    │ REST API
                                    │
┌─────────────────────┐             ▼
│   Web Frontend      │     ┌─────────────────────┐
│ React + Vite + TS   │────▶│    Spring Boot      │
│                     │     │      Backend        │
└─────────────────────┘     └──────────┬──────────┘
                                       │
                         ┌─────────────┼─────────────┐
                         │             │             │
                         ▼             ▼             ▼
                   ┌──────────┐  ┌──────────┐  ┌──────────┐
                   │  MySQL   │  │  Redis   │  │  Mail    │
                   │ Database │  │  Cache   │  │  Service │
                   └──────────┘  └──────────┘  └──────────┘
```

### Web Frontend / Back Office

The web application provides the administrative interface for interacting with the HDSS platform.

**Technologies:**

- React
- Vite
- TypeScript

### Backend

The backend provides the REST API consumed by both the web application and Android application.

**Technologies:**

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- MySQL
- Redis
- Lombok
- Springdoc / Swagger

### Android Application

The Android application provides a mobile interface for interacting with the HDSS platform, particularly for mobile and field-based workflows.

**Technologies:**

- Kotlin
- Jetpack Compose
- Android SDK

## Technology Stack

| Component | Technology |
|---|---|
| Web Frontend | React |
| Frontend Build Tool | Vite |
| Frontend Language | TypeScript |
| Backend | Java |
| Backend Framework | Spring Boot |
| ORM | JPA / Hibernate |
| Authentication | JWT |
| Database | MySQL |
| Cache | Redis |
| API Documentation | Swagger / OpenAPI |
| Boilerplate Reduction | Lombok |
| Android | Kotlin |
| Android UI | Jetpack Compose |
| Email | Spring Mail |
| Metrics | Micrometer / Spring Boot Actuator |
| Metrics Collection | Prometheus |
| Metrics Visualization | Grafana |

## Project Structure

The repository is organized into three main applications:

```text
hdss/
├── android/                   # Kotlin + Jetpack Compose application
├── backend/                   # Java + Spring Boot application
│   ├── downloads/             # hdss.apk served by the download endpoint
│   ├── run-local.sh            # Loads .env.local and starts Spring Boot
│   ├── .env.local.example     # Local backend environment template
│   ├── Dockerfile
│   └── .dockerignore
├── frontend/                  # React + Vite + TypeScript application
│   ├── nginx.conf             # Serves the production build, handles SPA routing
│   ├── Dockerfile
│   └── .dockerignore
├── database/
│   └── hdss.sql               # MySQL dump auto-imported on first container start
├── round-exports/             # Optional host-side round database exports (keep private)
├── monitoring/
│   ├── prometheus/
│   │   └── prometheus.yml     # Tells Prometheus what to scrape
│   └── grafana/
│       └── provisioning/
│           └── datasources/
│               └── datasource.yml  # Auto-registers Prometheus in Grafana
├── docker-compose.yml
├── .env                       # Docker Compose environment values (not committed)
└── .env.example                # Safe template for Docker Compose values
```

## Prerequisites

HDSS can be set up using **Docker (recommended)** or by installing each application's dependencies **manually**.

### Android (Always Required)

The Android application is not containerized and is always built and run through Android Studio, regardless of which setup path is used for the backend and frontend:

- Android Studio
- Android SDK
- Android emulator or physical Android device

### Docker (Recommended)

- Docker Engine
- Docker Compose (the `docker compose` plugin, not the older standalone `docker-compose` binary)

See [Docker Setup](#docker-setup) for installation instructions and the full setup process.

### Manual Setup (Alternative)

If not using Docker, each component is installed and run directly:

**Backend**

- JDK
- Maven
- MySQL
- Redis

**Frontend**

- Node.js
- npm

See [Manual Setup (Without Docker)](#manual-setup-without-docker) for details.

## Clone the Repository

Clone the repository and enter the project directory:

```bash
git clone <repository-url>
cd hdss
```

## Docker Setup

This is the recommended way to run HDSS. The backend and frontend are built and run as Docker containers, alongside MySQL and Redis containers, without installing MySQL, Redis, a JDK, or Node.js directly on your machine. For installing each component manually instead, see [Manual Setup (Without Docker)](#manual-setup-without-docker).

> The Android application is not containerized and is still built and run through Android Studio as described above.

### Before You Start: Required Files

Docker builds and starts the containers, but it does not create the following for you. Make sure each of these exists before running `docker compose up`:

- [ ] **`.env`** in the repository root — see [Environment Variables (.env)](#environment-variables-env)
- [ ] **`database/hdss.sql`** — a MySQL dump used to seed the database on first start — see [Database Dump](#database-dump)
- [ ] **`backend/downloads/hdss.apk`** — only required if the Android download endpoint will be used — see [Android APK in Docker](#android-apk-in-docker)

Without `.env`, Docker Compose will fail or fall back to empty values for required settings (database password, CORS origin, mail credentials). Without `database/hdss.sql`, the `mysql` container will start with an empty, unseeded database rather than failing outright — it is easy to miss that the dump was never imported.

### Docker Prerequisites

- Docker Engine
- Docker Compose (the `docker compose` plugin, not the older standalone `docker-compose` binary)

On Linux, Docker Engine and the Compose plugin are typically installed together via Docker's official apt repository. Refer to the [official Docker documentation](https://docs.docker.com/engine/install/) for your distribution.

After installing, verify both are available:

```bash
docker --version
docker compose version
```

### Docker Project Files

The Docker setup adds the following files to the repository, in addition to the `android/`, `backend/`, and `frontend/` application directories:

| File | Purpose |
|---|---|
| `docker-compose.yml` | Defines the `mysql`, `redis`, `backend`, `frontend`, `prometheus`, and `grafana` services |
| `backend/Dockerfile` | Multi-stage build: compiles the Spring Boot app with Maven and runs it on a JRE image with Oracle MySQL 8.4 client utilities (`mysql` and `mysqldump`) for round database copying |
| `backend/.dockerignore` | Excludes `target/`, `.git`, and IDE files from the build context |
| `frontend/Dockerfile` | Multi-stage build: builds the Vite app with Node, serves it with nginx |
| `frontend/nginx.conf` | Configures nginx to serve the built SPA and handle client-side routing |
| `frontend/.dockerignore` | Excludes `node_modules/`, `dist/`, and `.git` from the build context |
| `database/hdss.sql` | A MySQL dump used to seed the `mysql` container on first start |
| `monitoring/prometheus/prometheus.yml` | Tells Prometheus what to scrape — see [Observability (Prometheus & Grafana)](#observability-prometheus--grafana) |
| `monitoring/grafana/provisioning/datasources/datasource.yml` | Auto-registers Prometheus as a Grafana data source on first start |
| `.env` | Environment values consumed by `docker-compose.yml` (see below) |

### Environment Variables (.env)

`docker-compose.yml` reads environment-specific values from a `.env` file in the repository root. Docker Compose loads this file automatically. Create it from the committed example:

```bash
cp .env.example .env
```

Example root `.env`:

```dotenv
# MySQL root password
DB_PASSWORD=change_me

# The browser origin from which the frontend is served
CORS_ALLOWED_ORIGINS=http://localhost

# SMTP settings
MAIL_HOST=
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# Grafana initial admin credentials
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=change_me
```

Use a strong password for every environment that is not strictly local. Do not commit `.env`.

The root `.env` is for Docker Compose. It is **not automatically loaded** when Spring Boot is started directly with Maven. Local backend development uses `backend/.env.local`; see [Backend Setup](#backend-setup).

### Database Dump

The `mysql` service seeds its database from `database/hdss.sql` on first start, using MySQL's own `/docker-entrypoint-initdb.d/` mechanism. This only runs once, the first time the container initializes a fresh, empty data volume — it does **not** re-run on normal restarts of a container whose volume already has data, and it will not overwrite or update existing data in that volume.

To produce or refresh `database/hdss.sql` from an existing local MySQL database:

```bash
mysqldump -uroot -p<password> --single-transaction --databases hdss > database/hdss.sql
```

To force a re-import of the dump (for example, after updating it), the existing MySQL data volume must be removed first, since the import only happens against a fresh volume:

```bash
docker compose down -v
docker compose up -d
```

> **Warning:** `docker compose down -v` deletes every named volume defined in the Compose file, not just MySQL. This includes MySQL round databases and live data, Redis data, Prometheus data, and Grafana data. It does not delete files exported to the host under `round-exports/`. Only use it when you intentionally want to remove all container-managed data.

### Round Database Snapshots and Exports

HDSS can create a separate MySQL database for a surveillance round. The snapshot workflow copies the live database into a round-specific database on the MySQL server. Round metadata and the copy status are maintained by the backend.

The copy process runs from the backend container and invokes Oracle MySQL's `mysqldump` and `mysql` executables. This is why the backend runtime image installs the Oracle MySQL 8.4 Community Client; installing a client only in the `mysql` service is not sufficient.

#### Snapshot lifecycle

A round is created asynchronously. Its status progresses through:

- `CREATING` — the round record exists and the database copy is running.
- `COMPLETED` — the snapshot copy finished successfully.
- `FAILED` — validation or copying failed.

The backend serializes snapshot creation so that only one round-copy operation runs at a time. If copying fails, the incomplete destination database is removed and the round is marked failed.

#### List and inspect round databases

List round databases from the host:

```bash
docker exec -it hdss-mysql mysql -uroot -p \
  -e "SHOW DATABASES LIKE 'hdss_round%';"
```

Inspect a particular snapshot:

```bash
docker exec -it hdss-mysql mysql -uroot -p
```

Then, in the MySQL prompt:

```sql
USE hdss_round_YYYYMMDD_001;
SHOW TABLES;
```

Replace the example database name with the actual round database name.

#### Export a round database to the host

Round databases currently live inside the MySQL server. To retain an independent SQL export on the host, create the export directory and run `mysqldump` inside the MySQL container. Shell redirection writes the resulting file to the host:

```bash
mkdir -p round-exports

docker exec hdss-mysql sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers --events --databases hdss_round_YYYYMMDD_001' \
  | gzip > round-exports/hdss_round_YYYYMMDD_001.sql.gz
```

This command uses the MySQL container's `MYSQL_ROOT_PASSWORD` environment variable. Replace the database name in both places. Protect `round-exports/` because these files contain surveillance data.

To restore an export into the MySQL server:

```bash
set -a
source .env
set +a

gunzip -c round-exports/hdss_round_YYYYMMDD_001.sql.gz \
  | docker exec -i hdss-mysql mysql -uroot -p"$DB_PASSWORD"
```

The export includes database creation/use statements because `--databases` is used. The internal snapshot-copy operation is different: it copies into an already-created destination database and must not include the source database name in the dump stream.

> **Important persistence distinction:** Round databases stored inside MySQL are part of the `mysql_data` Docker volume. `docker compose down` preserves that volume; `docker compose down -v` deletes it. SQL files under the host's `round-exports/` directory are outside Docker volumes and remain on the host when containers and named volumes are removed.

The export procedure above is a manual operation. Creating a round does not, by itself, generate a compressed host-side export or automatically delete the round database after export. An automated export/retention worker would need to be implemented separately if that lifecycle is required.

### Android APK in Docker

As described in [Building the APK for Download](#building-the-apk-for-download), the backend serves the Android APK from a `downloads/hdss.apk` path relative to its working directory. The backend's `Dockerfile` copies the `backend/downloads/` directory into the image at the same relative path, so the existing download endpoint continues to work unchanged inside the container.

Build and place `hdss.apk` in `backend/downloads/` before building the backend image, exactly as described in the non-Docker setup above — the Docker image will not contain the APK if that directory is empty or missing when the image is built.

### Running the Stack

From the repository root, with `.env` and `database/hdss.sql` in place:

```bash
docker compose up -d --build
```

- `--build` builds (or rebuilds) the `backend` and `frontend` images from their Dockerfiles. Required on first run, and any time backend or frontend source code changes.
- `-d` runs the stack in detached mode, in the background, rather than blocking the terminal.

Once running:

| Service | Default URL |
|---|---|
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Frontend | `http://localhost` (or the host port configured in `docker-compose.yml`) |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

The `backend` service waits for `mysql` and `redis` to report healthy before starting, via Docker Compose's `depends_on` health-check conditions. On first start, this includes however long the `hdss.sql` import takes to complete.

### Viewing Logs

```bash
# All services, interleaved, following new output:
docker compose logs -f

# One service at a time:
docker compose logs -f backend
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f frontend

# Last 100 lines only, then follow:
docker compose logs -f --tail=100 backend
```

`Ctrl+C` stops watching the logs — it does not stop the containers.

### Common Docker Commands

```bash
docker compose ps               # list containers and their status/ports
docker compose down             # stop and remove containers; named volumes are kept
docker compose down -v          # also remove volumes — this deletes MySQL/Redis data
docker compose restart backend  # restart a single service without rebuilding
docker compose up -d --build    # rebuild and restart after source changes
```

### Verify the MySQL Client in the Backend Container

The round database copy process starts `mysqldump` and `mysql` through Java's `ProcessBuilder`. These executables must therefore exist **inside the backend container**, not only inside the MySQL database container or on the host.

The backend runtime image is based on Debian/Ubuntu rather than Alpine and installs the **Oracle MySQL 8.4 Community Client**. This avoids the MariaDB compatibility differences encountered when using Alpine's `mariadb-dump` and `mariadb` utilities.

After building the backend image, verify the client binaries:

```bash
docker exec hdss-backend mysqldump --version
docker exec hdss-backend mysql --version
```

Both commands should report MySQL Community client versions. If either command is missing, rebuild the backend image after correcting `backend/Dockerfile`:

```bash
docker compose build --no-cache backend
docker compose up -d backend
```

Keep the dump and import commands consistent with Oracle MySQL client options. In particular, `--set-gtid-purged=OFF` is an Oracle MySQL `mysqldump` option and should not be passed to MariaDB's dump utility.

### Port Conflicts

The `frontend` service is published on host port `80` by default. If another service is already using port 80 — for example, a system installation of Apache or nginx — `docker compose up` will fail with an error similar to:

```text
Error response from daemon: failed to set up container networking: driver failed programming external connectivity on endpoint hdss-frontend: failed to bind host port 0.0.0.0:80/tcp: address already in use
```

Identify what is using the port:

```bash
sudo ss -tlnp | grep :80
```

Either stop the conflicting service, or remap the frontend to a different host port in `docker-compose.yml`:

```yaml
frontend:
  ports:
    - "8081:80"
```

If the port is remapped, update `CORS_ALLOWED_ORIGINS` in `.env` to match (for example, `http://localhost:8081`), then restart:

```bash
docker compose up -d
```

## Observability (Prometheus & Grafana)

The Docker stack includes **Prometheus** for metrics collection and **Grafana** for visualization, scraping metrics directly from the Spring Boot backend via Micrometer and Actuator.

> This is only available when running the stack with Docker. It is not part of [Manual Setup (Without Docker)](#manual-setup-without-docker) — nothing stops it from being configured manually, but no instructions for doing so are provided here.

### Backend Dependencies

The backend exposes Prometheus-formatted metrics through Spring Boot Actuator and Micrometer's Prometheus registry:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Both are required. `spring-boot-starter-actuator` alone exposes metrics in Micrometer's own format, not Prometheus's — without `micrometer-registry-prometheus`, the `/actuator/prometheus` endpoint described below does not exist.

### Backend Configuration

Add to `application.properties`:

```properties
# 9. Observability / Actuator
management.endpoints.web.exposure.include=health,prometheus
management.endpoint.health.show-details=when-authorized
management.prometheus.metrics.export.enabled=true
management.metrics.tags.application=hdss-backend
```

`management.metrics.tags.application` attaches an `application="hdss-backend"` label to every metric the backend emits. This is required for the Grafana dashboard described below — without it, dashboards that filter or group by application have nothing to filter on, and every panel shows no data even though metrics are being collected correctly.

> Only `health` and `prometheus` are exposed, not every Actuator endpoint (`management.endpoints.web.exposure.include=*`). Exposing endpoints such as `/env` or `/beans` over HTTP is an information-disclosure risk in any environment reachable outside your own machine.

### Security Configuration

By default, Spring Security blocks unauthenticated access to all endpoints, including Actuator's. Prometheus scrapes `/actuator/prometheus` without authentication, so this path (and `/actuator/health`, for container healthchecks) must be explicitly permitted in the security configuration:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
    // ...existing rules
)
```

Without this, Prometheus's scrape requests return `403`, and the target shows as `DOWN` on Prometheus's own targets page even though the backend is otherwise working correctly.

### Prometheus Configuration

`monitoring/prometheus/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "hdss-backend"
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["backend:8080"]
```

`backend:8080` refers to the `backend` service by its Docker Compose service name, not `localhost` — Prometheus runs as its own container on the same Compose network, where service names resolve to each other directly.

### Grafana Configuration

`monitoring/grafana/provisioning/datasources/datasource.yml` auto-registers Prometheus as a Grafana data source on first start, so it does not need to be added manually through Grafana's UI:

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
```

### Accessing Prometheus and Grafana

| Service | Default URL |
|---|---|
| Prometheus | `http://localhost:9090` |
| Prometheus targets | `http://localhost:9090/targets` |
| Grafana | `http://localhost:3000` |

Log in to Grafana with the `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` values from `.env`.

### Verifying the Setup

Check each of the following in order — each one isolates a different layer, so working through them in sequence narrows down where a problem actually is rather than guessing:

1. **The backend's own endpoint.** `curl http://localhost:8080/actuator/prometheus` from the host. A `403` means the Security configuration above is missing; a `404` means the Micrometer Prometheus dependency is missing.
2. **Prometheus is scraping successfully.** Open `http://localhost:9090/targets`. The `hdss-backend` job should show State: **UP**.
3. **Grafana can query Prometheus.** In Grafana, open **Explore**, select the Prometheus data source, and query a basic metric such as `jvm_memory_used_bytes`. Real data points confirm the full chain is working.

### Importing a Dashboard

**Dashboards → New → Import**, using dashboard ID **4701** ("JVM (Micrometer)") provides JVM memory, GC, HTTP request, and DB connection pool panels without building them manually.

**Known gotchas**, found when first setting this up:

- **Panels show "N/A" for every value.** This almost always means `management.metrics.tags.application` (above) was added after the dashboard was first imported, or is missing entirely — the dashboard's `$application` variable has nothing to select, because none of the scraped metrics carry an `application` label yet.
- **`$application` has a value selected, but panels still show no data.** Check the dashboard's second variable (commonly `$instance`). Some imported dashboards define it as `label_values(up{application=~"$application"}, instance)` — but Prometheus's own `up` metric is synthesized by Prometheus itself per scrape target and never carries the `application` tag, since that tag is only attached to metrics the backend actually emits. This makes the `$instance` dropdown permanently empty. Fix by changing that variable's query to `label_values(up, instance)` instead, under dashboard settings → **Variables**.
- **Panels only update when their query is run manually.** The dashboard's refresh interval (top-right, next to the time range picker) is likely set to **Off**. Set it to an interval such as `10s`.
- **A variable's dropdown doesn't pick up newly available values on its own.** Check that variable's **Refresh** setting under dashboard settings → **Variables** — if it is set to **Never**, change it to **On Dashboard Load**.

## Manual Setup (Without Docker)

This is the alternative to [Docker Setup](#docker-setup) — use this if you prefer to install and run the backend and frontend directly, rather than as containers. [Clone the repository](#clone-the-repository) first if you have not already.

### Backend Setup

The backend runs on port **8080** by default and requires Java, Maven, MySQL, and Redis.

#### Prerequisites

- JDK 21
- Maven 3.9 or later
- MySQL 8.4 (or a compatible MySQL server)
- Redis 7 or later

Install and start MySQL and Redis locally before starting the backend. Create the application database if it does not already exist:

```sql
CREATE DATABASE hdss;
```

#### Configure Local Environment

The backend reads its configuration from Spring Boot's `application.properties`, with environment variables overriding the defaults. For local development, use the provided environment template:

```bash
cd backend
cp .env.local.example .env.local
```

Edit `.env.local` and set the database credentials, Redis connection, mail settings, CORS origin, and other values for your machine.

Example:

```dotenv
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hdss?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=change_me

SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173

MAIL_HOST=
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

JWT_EXPIRATION_TIME=86400000
JWT_ISSUER=hdss_auth
JWT_REFRESH_EXPIRATION=2592000000

HDSS_ROUND_DATABASE_PREFIX=hdss_round
HDSS_ROUND_COPY_TIMEOUT_MINUTES=30
```

The complete template is in `backend/.env.local.example`. Do not commit `.env.local`.

#### Start the Backend

The repository includes `run-local.sh`. It loads `.env.local` and starts Spring Boot:

```bash
chmod +x run-local.sh
./run-local.sh
```

The script runs:

```bash
mvn spring-boot:run -DskipTests
```

Alternatively, load the environment manually:

```bash
set -a
source .env.local
set +a
mvn spring-boot:run -DskipTests
```

Spring Boot does not automatically load a file named `.env.local`; the script or the manual `source` command is what makes those variables available to the process.

The API will be available at:

```text
http://localhost:8080
```

### Frontend Setup

From the repository root:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Vite will display the frontend URL in the terminal. The default Vite development URL is commonly:

```text
http://localhost:5173
```

The exact URL depends on the Vite configuration.


### Android Setup

Open the `android` directory in **Android Studio**.

Allow Android Studio to:

1. Sync the Gradle project.
2. Download the required dependencies.
3. Configure the required Android SDK components.

Run the application using an Android emulator or a connected physical Android device.

#### Building the APK for Download

The HDSS backend can serve the Android application as a downloadable APK. Before the download endpoint can be used, the APK must first be built and placed in the backend's `downloads` directory.

1. Open the `android` project in **Android Studio**.
2. Build a **debug APK**. A debug APK is recommended for development and testing.
3. Create a `downloads` directory at the root of the backend project:

```text
hdss/
├── android/
├── backend/
│   └── downloads/
└── frontend/
```

4. Copy the generated APK into the `backend/downloads/` directory.
5. Rename the APK to exactly:

```text
hdss.apk
```

The resulting structure should be:

```text
backend/
├── downloads/
│   └── hdss.apk
└── ...
```

The backend download service uses this file when serving the Android application for download.

> **Note:** The `downloads` directory and `hdss.apk` should be present before using the Android application download endpoint. For development, a debug APK is sufficient. Do not use a debug APK for production distribution.

#### Backend Connection

The Android application must be configured with the correct backend API URL.

When using an Android emulator, `localhost` refers to the emulator itself rather than the development machine. For the standard Android emulator, the host machine can generally be reached using:

```text
http://10.0.2.2:8080
```

The appropriate API URL depends on the Android environment and the application's networking configuration.

## Configuration

The backend uses Spring Boot's `application.properties` configuration.

The backend keeps non-secret defaults and environment-variable placeholders in:

```text
backend/src/main/resources/application.properties
```

For local development, copy `backend/.env.local.example` to `backend/.env.local` and run the backend through `backend/run-local.sh`. Docker Compose supplies its own environment variables from the root `.env` file and the Compose service configuration.

Spring Boot resolves placeholders in the form `${ENVIRONMENT_VARIABLE:default}`. Environment variables override values in `application.properties`.

### Example Configuration

```properties
spring.application.name=

# 1. JWT Settings
jwt.expiration.time=
jwt.issuer=
jwt.refresh-expiration=

# 2. Database Connection Settings
spring.datasource.url=jdbc:mysql://localhost:3306/<database_name>?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 3. JPA / Hibernate Configurations
spring.jpa.hibernate.ddl-auto=
spring.jpa.show-sql=
spring.jpa.open-in-view=false

# 4. Mail Configurations
spring.mail.host=
spring.mail.port=
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=
spring.mail.properties.mail.smtp.starttls.enable=

# 5. CORS Configuration
app.cors.allowed-origins=

# 6. Cache Configuration
spring.cache.type=redis
spring.data.redis.host=
spring.data.redis.port=
spring.data.redis.timeout=
spring.cache.redis.time-to-live=
spring.cache.redis.cache-null-values=false

# 7. Swagger Configuration
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true

# 8. Security Configuration
logging.level.org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer=ERROR
```

### Configuration Categories

| Category | Purpose |
|---|---|
| JWT Settings | Authentication and token expiration |
| Database | MySQL connection settings |
| JPA / Hibernate | ORM and database behavior |
| Mail | SMTP/email server configuration |
| CORS | Allowed frontend origins |
| Redis | Application caching |
| Swagger | API documentation |
| Security | Spring Security logging configuration |

### Secrets and Sensitive Configuration

Do not commit sensitive values to the repository.

In particular, keep the following out of version control:

- Database passwords
- JWT secrets
- Email passwords
- API keys
- Production credentials
- Android signing credentials
- Other environment-specific secrets

Commit `.env.example` and `backend/.env.local.example`, but keep the actual `.env` and `backend/.env.local` files out of version control. Example files should contain safe placeholders, never real credentials.

## Database

HDSS uses **MySQL** as its primary relational database.

> If running the stack with Docker, MySQL is provided as a container and seeded automatically from `database/hdss.sql` — see [Database Dump](#database-dump). The steps below apply to a manually installed, non-containerized MySQL instance.

Create a database before starting the backend.

For example:

```sql
CREATE DATABASE <database_name>;
```

Then configure the connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<database_name>?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=<username>
spring.datasource.password=<password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### Hibernate Configuration

The database schema behavior is controlled through:

```properties
spring.jpa.hibernate.ddl-auto=
```

Common values include:

```text
none
validate
update
create
create-drop
```

Use the value appropriate for the environment. Avoid destructive schema-generation settings in production unless they are explicitly intended.

## Redis

**Redis** is used by the backend for application caching.

> If running the stack with Docker, Redis is provided as a container and requires no separate installation — see [Docker Setup](#docker-setup). The steps below apply to a manually installed, non-containerized Redis instance.

Configure Redis using:

```properties
spring.cache.type=redis
spring.data.redis.host=
spring.data.redis.port=
spring.data.redis.timeout=
spring.cache.redis.time-to-live=
spring.cache.redis.cache-null-values=false
```

For a local Redis installation, the configuration will commonly be:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Make sure Redis is running before starting the backend if the application requires Redis during startup.

## API Documentation

HDSS uses **Springdoc / Swagger** to provide interactive API documentation.

Swagger can be enabled using:

```properties
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

With the backend running on port 8080, Swagger UI is typically available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI specification is typically available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger provides an interactive way to:

- Explore API endpoints
- View request and response models
- Inspect authentication requirements
- Test API endpoints
- Understand the backend API contract

The exact paths may vary depending on the Springdoc configuration and version used by the project.

## Authentication and Security

The backend uses **JWT (JSON Web Tokens)** for authentication.

Relevant configuration includes:

```properties
jwt.expiration.time=
jwt.issuer=
jwt.refresh-expiration=
```

The application uses access and refresh token expiration settings to manage authenticated sessions.

The exact authentication flow and roles/permissions depend on the implementation of the backend.

## CORS Configuration

The backend supports Cross-Origin Resource Sharing through:

```properties
app.cors.allowed-origins=
```

For local development, this may be configured as:

```properties
app.cors.allowed-origins=http://localhost:5173
```

For production, configure this with the actual frontend domain rather than allowing unrestricted origins.

## Mail Configuration

Email functionality is configured through Spring Mail:

```properties
spring.mail.host=
spring.mail.port=
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=
spring.mail.properties.mail.smtp.starttls.enable=
```

Populate these values according to the SMTP provider used by the deployment environment.

## Development

The three applications can be run independently during development.

### Backend

From `backend/`, configure `.env.local` as described in [Backend Setup](#backend-setup), then run:

```bash
./run-local.sh
```

Backend API:

```text
http://localhost:8080
```

### Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

### Android

Open `android/` in Android Studio and run the application on an emulator or connected Android device.

### Typical Development Setup

A complete local development environment may look like:

```text
Frontend
    │
    │ HTTP/REST
    ▼
Backend :8080
    │
    ├── MySQL
    │
    ├── Redis
    │
    └── SMTP Server

Android App
    │
    │ HTTP/REST
    ▼
Backend :8080
```

## Building for Production

### Backend

Build the Spring Boot application:

```bash
./mvnw clean package
```

The generated JAR will typically be placed in:

```text
backend/target/
```

Run the application with:

```bash
java -jar target/<application-name>.jar
```

The exact JAR filename depends on the project's Maven configuration.

### Frontend

From `frontend/`:

```bash
npm install
npm run build
```

The production build is typically generated in:

```text
frontend/dist/
```

The generated files can be served using an appropriate web server or static hosting solution.

### Android

The Android application can be built using Android Studio or Gradle.

For example:

```bash
./gradlew assembleDebug
```

For a release build, configure the appropriate signing credentials and release configuration before generating the APK/AAB.

## Contributing

Contributions are welcome.

Before submitting a contribution:

1. Create a new branch for your work.
2. Make your changes.
3. Test the affected application(s).
4. Ensure sensitive configuration is not committed.
5. Update documentation where appropriate.
6. Submit a pull request describing your changes.

Example:

```bash
git checkout -b feature/my-new-feature
```

After making your changes:

```bash
git add .
git commit -m "Add my new feature"
git push origin feature/my-new-feature
```

Then open a pull request against the appropriate branch.

## Security Considerations

When deploying HDSS, ensure that sensitive configuration is protected.

Do not commit production credentials or secrets to the repository.

In particular, review:

- Database credentials
- JWT configuration and secrets
- SMTP credentials
- Redis configuration
- CORS origins
- Production API endpoints
- Android signing credentials
- Environment-specific configuration
- Round database SQL exports (for example, files under `round-exports/`)

If running the stack with Docker, the same principle applies to the `.env` file described in [Environment Variables (.env)](#environment-variables-env) — do not commit it, and do not reuse development values (such as the example `DB_PASSWORD`) in any environment that is reachable outside your own machine.

Production deployments should use HTTPS/TLS and appropriate secret and credential management.

## License

This project is licensed under the terms specified in the repository's license.

If a license has not yet been selected, add an appropriate `LICENSE` file before distributing the project publicly.

## Project Status

HDSS is under active development.

The architecture and available functionality may evolve as new health, demographic, data collection, analysis, and administrative requirements are implemented.