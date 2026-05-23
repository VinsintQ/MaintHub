# MaintHub

MaintHub is an Equipment Damage and Maintenance Management System built with Spring Boot. It helps organizations track equipment, report damage, assign technicians, manage maintenance tasks, request spare parts, inspect repaired equipment, and keep a full history of equipment status changes.

The backend includes JWT authentication, role-based authorization, email verification, and workflow modules for staff, admins, technicians, and inspectors. The system is designed so equipment status changes happen through service workflows and are recorded automatically in status history.

## Tools and Technologies

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring Security
- JWT authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Lombok
- Jakarta Bean Validation
- Spring Mail
- Docker and Docker Compose
- Postman

## General Approach

The project was built around the existing authentication system instead of replacing it. The auth layer already handled registration, login, JWT generation, role-based access, and email verification, so the maintenance modules were added around that structure. Existing users and roles were extended to support the MaintHub workflow roles: admin, staff, technician, and inspector.

The main backend design uses a service-first workflow approach. Controllers receive DTOs, services enforce business rules, repositories handle persistence, and entities model the relationships between equipment, reports, tasks, inspections, spare part requests, and status history. Equipment status is not meant to be changed directly from controllers; service methods update it and create an `EquipmentStatusHistory` record at the same time.






## Installation

## Seed Data

The project includes an automatic seed file:

```text
src/main/java/com/MaintHub/demo/config/DatabaseSeeder.java
```

It runs when the application starts and inserts default roles, users, equipment categories, and sample equipment if they do not already exist.

Default seeded users:

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@mainthub.com | Password123! |
| Staff | staff@mainthub.com | Password123! |
| Technician | technician@mainthub.com | Password123! |
| Inspector | inspector@mainthub.com | Password123! |

Seeded equipment categories:

- Heavy Equipment
- Safety Equipment
- IT Equipment

### Requirements

- Java 17 or later
- Maven, or the included Maven wrapper
- PostgreSQL
- Docker Desktop, optional
- Postman, optional for API testing

### Run Locally

1. Create a PostgreSQL database.

```sql
CREATE DATABASE last;
```

2. Update database settings in:

```text
src/main/resources/application.properties
```

3. Run the project:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Run tests:

```powershell
.\mvnw.cmd test
```

### Run with Docker

1. Copy the example environment file if you want custom values:

```powershell
Copy-Item .env.example .env
```

2. Start the API and PostgreSQL:

```powershell
docker compose up --build
```

3. The API will run at:

```text
http://localhost:8080
```

PostgreSQL is exposed on host port `5433` by default.

### Postman Collection

Import this file into Postman:

[MaintHub Postman Collection](./postman/MaintHub.postman_collection.json)

The collection uses `{{baseUrl}}` and `{{jwtToken}}`. After running the login request, the JWT token is saved automatically into the collection variables.
