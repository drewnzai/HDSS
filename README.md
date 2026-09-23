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
└── frontend/                  # React + Vite + TypeScript application
```

## Prerequisites

Before setting up the project, make sure the following are installed.

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

#### Backend Connection

The Android application must be configured with the correct backend API URL.

When using an Android emulator, `localhost` refers to the emulator itself rather than the development machine. For the standard Android emulator, the host machine can generally be reached using:

```text
http://10.0.2.2:8080
```

The appropriate API URL depends on the Android environment and the application's networking configuration.

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

Production deployments should use HTTPS/TLS and appropriate secret and credential management.

## License

This project is licensed under the terms specified in the repository's license.

If a license has not yet been selected, add an appropriate `LICENSE` file before distributing the project publicly.

## Project Status

HDSS is under active development.

The architecture and available functionality may evolve as new health, demographic, data collection, analysis, and administrative requirements are implemented.
