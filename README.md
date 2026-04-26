# 🔗 SouvaLinker – Scalable URL Shortener with Secure Auth

Backend system built with Spring Boot that provides URL shortening along with a secure, scalable authentication system.

This project demonstrates real-world backend engineering practices including event-driven design, token security, rate limiting, and observability.

---

## Architecture Diagram

<img width="717" height="796" alt="souvalinker-infra-aws-architecture" src="https://github.com/user-attachments/assets/3b737eae-a4e9-4600-b64d-dc02a07b34e9" />

## Architecture includes:

- Amazon CloudFront (edge caching)
- Application Load Balancer (ALB)
- Amazon ECS Fargate (multi-AZ)
- ECS Service Auto Scaling
- Amazon RDS PostgreSQL (Primary)
- Amazon RDS Read Replica
- Amazon ElastiCache Redis
- Amazon SES
- Bastion Host (restricted SSH)
- CloudWatch Logs / Dashboards / Alarms

Traffic flow:

```text
Users
→ CloudFront
→ ALB
→ ECS Fargate
→ Redis / RDS / SES
```

---

## Features

## Authentication

- User registration
- Login
- JWT authentication
- Email verification
- Password reset flow

---

## URL Shortening

- Base62 short code generation
- Short URL creation
- Redirect resolution

---

## Multi-Level Caching

### L1 Cache

Caffeine (application-level)

```text
Fastest lookup
In-memory
```

---

### L2 Cache

Redis

```text
Shared distributed cache
Cross-instance consistency
```

---

### Resolution Flow

```text
Request
→ L1 Caffeine
→ L2 Redis
→ Read Replica
```

---

## Read Scaling

- Primary database for writes
- Read replica for read traffic
- Routing datasource with annotation-driven replica routing

```java
@ReadOnlyReplica
```

---

## Rate Limiting

Redis-backed scalable rate limiting:

Protected endpoints:

- Login
- Register
- Forgot Password
- Short URL creation
- Redirect endpoint

Implemented using:

- Custom annotation
- Interceptor
- Strategy pattern

---

## Observability

## Structured Logging

- Correlation IDs
- CloudWatch-ready logs
- Structured event logging

---

## Metrics

Micrometer metrics:

```text
short_url_created_total
redirect_resolution_total
rate_limit_rejections_total
redirect_resolution_latency
```

---

## Tracing

OpenTelemetry tracing configuration enabled.

---

## Health Checks

Dependency health includes:

- Primary DB
- Read Replica
- Redis

```text
/actuator/health
```

---

## Tech Stack

## Backend

- Java 21
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Caffeine Cache

---

## AWS

- ECS Fargate
- RDS
- ElastiCache
- SES
- CloudFront
- CloudWatch

---

## DevOps

- Docker
- Terraform (in progress)
- GitHub Actions / CI (planned)

---

## Run Locally

## Clone

```bash
git clone https://github.com/your-username/souvalinker.git

cd souvalinker
```

---

## Build

```bash
mvn clean package
```

---

## Run

```bash
mvn spring-boot:run
```

---

## Docker

Build:

```bash
docker build -t souvalinker .
```

Run:

```bash
docker run -p 8080:8080 souvalinker
```

---

## Roadmap

## In Progress

- Terraform infrastructure modules
- ECS deployment
- AWS networking setup

---

## Planned

- Route 53
- AWS WAF
- CloudFront advanced caching
- Blue/Green deployment
- CI/CD pipeline

---

## Repository Structure

```text
src/
├── controller/
├── service/
├── repository/
├── security/
├── cache/
├── metrics/
├── health/
├── strategy/
└── config/

Dockerfile
aws_url_shortener_proper_icons.html
README.md
```

---

## Design Goals

This project focuses on:

- Scalability
- Resilience
- Observability
- Cloud-native deployment
- Real-world backend architecture

---

## License

MIT License

---

## Author

Souvanik Saha

