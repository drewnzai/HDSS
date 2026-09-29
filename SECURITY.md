# Security Policy

## Supported Versions

Security updates are generally applied to actively maintained versions of HDSS.

| Version        | Supported |
| -------------- | --------- |
| Latest         | Yes       |
| Older versions | May vary  |

For information about whether a particular release is currently maintained, contact the project maintainers.

## Reporting a Security Vulnerability

Please **do not report security vulnerabilities through public GitHub issues**.

If you discover a potential security vulnerability, contact the project maintainers privately with enough information to reproduce and investigate the issue.

A security report should include:

* A description of the vulnerability.
* The affected component (`android`, `backend`, or `frontend`).
* Steps required to reproduce the issue.
* The potential impact.
* Relevant logs or proof-of-concept information, where appropriate.
* Any suggested mitigation, if known.

Please do not include real passwords, authentication tokens, private keys, personal information, or other sensitive production data in the report.

## Security-Sensitive Areas

Particular care should be taken when modifying:

* Authentication and authorization
* JWT handling
* Password management
* Database access
* File uploads and downloads
* API endpoints
* CORS configuration
* Redis configuration
* Email functionality
* Android authentication and API communication
* Production configuration

## Secrets

Never commit secrets to the repository.

Examples include:

```text
Passwords
JWT secrets
API keys
SMTP credentials
Private keys
Android signing keys
Access tokens
Database credentials
```

The repository's example configuration files should contain property names and safe placeholder values rather than real credentials.

## Dependency Security

Keep project dependencies reasonably up to date and address known security vulnerabilities in dependencies when practical.

Security-related dependency updates should be tested before deployment.

## Responsible Disclosure

Security issues should be disclosed privately to the maintainers before public disclosure whenever possible. This gives the maintainers an opportunity to investigate and address the issue.
