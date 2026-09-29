# Contributing to HDSS

Thank you for your interest in contributing to the **Health and Demographic Surveillance System (HDSS)** project.

Contributions help improve the platform and make it more useful for health and demographic surveillance workflows.

## Getting Started

Before contributing:

1. Fork the repository.
2. Clone your fork.
3. Create a new branch for your changes.
4. Set up the required development environment.
5. Make and test your changes.
6. Submit a pull request.

## Repository Structure

The project consists of three main applications:

```text
hdss/
├── android/       # Kotlin + Jetpack Compose
├── backend/       # Java + Spring Boot
└── frontend/      # React + Vite + TypeScript
```

Changes should generally be made within the appropriate application directory.

## Branches

Create a descriptive branch for your work.

Examples:

```bash
git checkout -b feature/add-household-search
git checkout -b fix/login-token-expiry
git checkout -b refactor/location-service
```

Prefer concise branch names that describe the purpose of the change.

## Commits

Write clear and descriptive commit messages.

For example:

```text
Add household search endpoint
Fix JWT refresh token handling
Update location management UI
Add Android visit synchronization
```

Avoid vague commit messages such as:

```text
fix stuff
changes
update
```

## Pull Requests

Before opening a pull request:

* Ensure the project builds successfully.
* Run the relevant tests.
* Check for compiler and lint errors.
* Verify that the affected functionality works as expected.
* Update documentation when necessary.
* Ensure that no secrets or credentials are included.
* Keep unrelated changes out of the pull request.

The pull request description should explain:

* What was changed.
* Why it was changed.
* How it was tested.
* Any additional considerations reviewers should know about.

## Backend Contributions

The backend is implemented using Java and Spring Boot.

When modifying the backend:

* Follow the existing project structure.
* Keep business logic in appropriate service classes.
* Keep controllers focused on HTTP/API concerns.
* Use DTOs where appropriate.
* Add or update tests for changed functionality.
* Avoid committing local configuration files containing secrets.

The backend uses:

* Spring Boot
* Spring Data JPA / Hibernate
* Spring Security
* JWT
* MySQL
* Redis
* Lombok
* Swagger / OpenAPI

## Frontend Contributions

The frontend is implemented using React, Vite, and TypeScript.

When modifying the frontend:

* Follow the existing component structure.
* Prefer reusable components over duplicated UI code.
* Keep TypeScript types explicit and meaningful.
* Follow the existing styling and design system.
* Test responsive layouts where applicable.
* Keep API communication consistent with the existing frontend architecture.

## Android Contributions

The Android application is implemented using Kotlin and Jetpack Compose.

When modifying the Android application:

* Follow existing Kotlin conventions.
* Prefer Jetpack Compose for UI implementation.
* Keep UI and business logic appropriately separated.
* Add or update tests where applicable.
* Test changes on an emulator or physical Android device where appropriate.

## Configuration and Secrets

Never commit sensitive configuration to the repository.

Do not commit:

* Database passwords
* JWT secrets
* SMTP passwords
* API keys
* Production credentials
* Android signing credentials
* Private keys
* Personal access tokens

Use the provided configuration examples as a reference.

## Reporting Bugs

Before reporting a bug, check whether the issue has already been reported.

When reporting a bug, provide:

* A clear description of the problem.
* Steps to reproduce it.
* Expected behavior.
* Actual behavior.
* Relevant logs or error messages.
* The affected component (`android`, `backend`, or `frontend`).
* Relevant environment information.

Do not include passwords, tokens, private keys, personal information, or other sensitive data in an issue.

## Feature Requests

Feature requests should explain:

* The problem being addressed.
* The proposed functionality.
* Why the functionality would be useful.
* Any relevant implementation considerations.

## Code of Conduct

All contributors are expected to follow the project's [Code of Conduct](CODE_OF_CONDUCT.md).

## License

By contributing to HDSS, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
