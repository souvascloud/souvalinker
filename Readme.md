# Souvalinker

URL Shortener backend built with Java + Spring Boot.

## Features

### Authentication
- User registration
- Email verification
- JWT authentication
- Forgot password
- Reset password

### URL Shortening
- Create short URLs
- Redirect to original URLs
- User ownership of URLs

### Performance
- Redis cache for short URL resolution
- Read replica routing design
- Async email sending

### Security
- JWT authentication filter
- Password hashing (BCrypt)
- Global exception handling

### Observability
- Spring Boot Actuator
- Custom health checks
- Info endpoint
- Structured logging

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- PostgreSQL
- Redis
- AWS SES
- JPA / Hibernate
- Maven

## Architecture (Current)

```text
Client
 ↓
ALB (planned)
 ↓
Spring Boot App
 ↓
 ├── PostgreSQL Primary + Read Replica
 ├── Redis / ElastiCache
 └── AWS SES
```

## Planned Next Steps

- Redis-backed rate limiting
- Terraform infrastructure
- ECS deployment
- CloudWatch monitoring
- GitHub Actions CI/CD

## Run

```bash
mvn clean install
mvn spring-boot:run
```