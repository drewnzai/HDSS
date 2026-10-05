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
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
  - [Android Setup](#android-setup)
- [Docker Setup](#docker-setup)
  - [Docker Prerequisites](#docker-prerequisites)
  - [Docker Project Files](#docker-project-files)
  - [Environment Variables (.env)](#environment-variables-env)
  - [Database Dump](#database-dump)
  - [Android APK in Docker](#android-apk-in-docker)
  - [Running the Stack](#running-the-stack)
  - [Viewing Logs](#viewing-logs)
  - [Common Docker Commands](#common-docker-commands)
  - [Port Conflicts](#port-conflicts)
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

## Project Structure

The repository is organized into three main applications:

```text
hdss/
├── android/                   # Kotlin + Jetpack Compose application
├── backend/                   # Java + Spring Boot application
│   ├── downloads/             # hdss.apk served by the download endpoint
│   ├── Dockerfile
│   └── .dockerignore
├── frontend/                  # React + Vite + TypeScript application
│   ├── nginx.conf             # Serves the production build, handles SPA routing
│   ├── Dockerfile
│   └── .dockerignore
├── database/
│   └── hdss.sql               # MySQL dump auto-imported on first container start
├── docker-compose.yml
└── .env                       # Docker Compose environment values (not committed)
```

## Prerequisites

Before setting up the project, make sure the following are installed.

> As an alternative to installing MySQL, Redis, a JDK, and Node.js directly, the backend, frontend, MySQL, and Redis can all be run as Docker containers instead. See [Docker Setup](#docker-setup).

### Backend

- JDK
- Maven
- MySQL
- Redis

### Frontend

- Node.js
- npm

### Android

- Android Studio
- Android SDK
- Android emulator or physical Android device

## Getting Started

### Clone the Repository

Clone the repository and enter the project directory:

```bash
git clone <repository-url>
cd hdss
```

### Backend Setup

Navigate to the backend directory:

```bash
cd backend
```

The backend runs on port **8080** by default.

#### Configure Application Properties

A sample configuration file is provided in the repository as:

```text
application.properties.example
```

Copy it to the Spring Boot resources directory:

```bash
cp application.properties.example src/main/resources/application.properties
```

Then populate the required configuration values.

> Do not commit your local `application.properties` if it contains passwords, secrets, API keys, or other sensitive values.

#### Start the Backend

If the project uses the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed globally:

```bash
mvn spring-boot:run
```

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

## Docker Setup

As an alternative to running the backend and frontend directly with Maven and npm, both can be built and run as Docker containers, alongside MySQL and Redis containers. This is the recommended way to get a complete local stack running quickly without installing MySQL, Redis, a JDK, or Node.js directly on your machine.

> The Android application is not containerized and is still built and run through Android Studio as described above.

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
| `docker-compose.yml` | Defines the `mysql`, `redis`, `backend`, and `frontend` services |
| `backend/Dockerfile` | Multi-stage build: compiles the Spring Boot app with Maven, runs it on a JRE image |
| `backend/.dockerignore` | Excludes `target/`, `.git`, and IDE files from the build context |
| `frontend/Dockerfile` | Multi-stage build: builds the Vite app with Node, serves it with nginx |
| `frontend/nginx.conf` | Configures nginx to serve the built SPA and handle client-side routing |
| `frontend/.dockerignore` | Excludes `node_modules/`, `dist/`, and `.git` from the build context |
| `database/hdss.sql` | A MySQL dump used to seed the `mysql` container on first start |
| `.env` | Environment values consumed by `docker-compose.yml` (see below) |

### Environment Variables (.env)

`docker-compose.yml` reads sensitive and environment-specific values from a `.env` file in the repository root. This file is not committed to version control — create it yourself before running the stack.

```dotenv
# MySQL root password — used both by the mysql container itself and by
# the backend's datasource connection.
DB_PASSWORD=password

# The origin the frontend is actually served from, matching
# app.cors.allowed-origins in application.properties.
# Use http://localhost if the frontend container is published on port 80,
# or http://localhost:8081 (or whichever host port you mapped it to).
CORS_ALLOWED_ORIGINS=http://localhost

# SMTP credentials, matching spring.mail.username / spring.mail.password.
MAIL_USERNAME=
MAIL_PASSWORD=
```

> Add `.env` to `.gitignore` if it is not already excluded. Treat its contents the same as any other secret — do not commit real passwords or SMTP credentials.

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

> `down -v` deletes the named volumes defined in `docker-compose.yml`, including the MySQL data volume. Only do this if you intend to discard the current state of the containerized database and reseed it from `database/hdss.sql`.

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

## Configuration

The backend uses Spring Boot's `application.properties` configuration.

The repository provides an example configuration file:

```text
application.properties.example
```

The example contains the required property names without requiring developers to commit environment-specific credentials.

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

The `application.properties.example` file should contain property names and safe example/default values only.

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

From `backend/`:

```bash
./mvnw spring-boot:run
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

If running the stack with Docker, the same principle applies to the `.env` file described in [Environment Variables (.env)](#environment-variables-env) — do not commit it, and do not reuse development values (such as the example `DB_PASSWORD`) in any environment that is reachable outside your own machine.

Production deployments should use HTTPS/TLS and appropriate secret and credential management.

## License

This project is licensed under the terms specified in the repository's license.

If a license has not yet been selected, add an appropriate `LICENSE` file before distributing the project publicly.

## Project Status

HDSS is under active development.

The architecture and available functionality may evolve as new health, demographic, data collection, analysis, and administrative requirements are implemented.
