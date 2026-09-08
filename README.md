# NexVerse

NexVerse is an enterprise EdTech/LMS platform for organizations to manage employees, departments, managers, courses, learning content, assignments, access requests, enrollments, and learning progress. It has role-based experiences for organization admins, managers, and employees — managers manage their teams while also having their own learner experience.

Courses are structured into modules and content, and follow a lifecycle from creation and content management through validation, preview, and publishing. Course visibility and access are governed by organizational and departmental rules. Employees browse eligible courses, receive or request access, enroll, consume content, and track progress and learning activity; dashboards surface learning and management insights per role.

## Repository layout

This is two independent projects in one root, not a monorepo build:

- [`nexverse/`](./nexverse) — Spring Boot 3.5 (Java 17) REST API, Maven.
- [`nexverse-fe/`](./nexverse-fe) — Angular 19 SPA (standalone components, Angular Material), served separately.

The frontend talks to the backend via `apiUrl` in `nexverse-fe/src/environments/environment.*.ts` (`http://localhost:8080/api/v1` in dev).

## Backend (`nexverse/`)

- Run: `./mvnw spring-boot:run`
- Build: `./mvnw clean package`
- Test: `./mvnw test`
- Local infra (MySQL, Mailpit for dev email, FTP): `docker compose -f docker-compose.ftp.yml up -d`

All secrets/config live in `application.properties` as `${ENV_VAR}` placeholders, backed by a local `.env` (not committed). See required vars in `nexverse/CLAUDE.md`.

Layered architecture per feature: `controller` → `service` → `service.impl` → `repository` (Spring Data JPA), with stateless JWT auth, multi-tenant `Organization` → `Department` → `User` hierarchy, and course/module/content structure with parallel per-user progress entities. API docs at `/swagger-ui.html`.

## Frontend (`nexverse-fe/`)

- Dev server: `npm start` (or `ng serve`) → `http://localhost:4200`
- Build: `npm run build`
- Test: `npm test`

Standalone-components Angular app, routes lazy-load via `loadComponent`. Structure: `core/` (interceptors, guards, services, models), `components/` (feature areas mirroring backend domains), `shared/` (cross-feature reusable components/pipes). Dashboards are role-specific components selected at runtime based on the logged-in user's role.

## Domain model

Identity, organization membership, visibility, access, enrollment, and progress are distinct concepts: being visible/eligible for a course is not the same as having access to it, which is not the same as being enrolled in it, which is not the same as having progress on it.
