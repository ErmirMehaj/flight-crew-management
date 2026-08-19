# Flight Crew Management System

A backend REST API for managing an airline's pilots, cabin crew, aircraft, and flights, built from scratch as a learning project to practice production-style Spring Boot architecture, JPA relationship modeling, and business-rule-driven service design.

It's not just CRUD. The interesting part is the scheduling logic: pilots can't be double-booked or assigned while unavailable, aircraft can't be assigned to overlapping flights, crew members can't be scheduled past a configurable weekly hour limit, and completing a flight has real side effects (crediting pilot flight hours, logging crew work hours) rather than just flipping a status flag.

## Tech stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 21, Spring Boot 3.5 |
| Persistence | Spring Data JPA (Hibernate), PostgreSQL |
| Build | Maven |
| Validation | Jakarta Bean Validation (`@Valid`, cross-field `@AssertTrue`) |
| API docs | springdoc-openapi / Swagger UI |
| Testing | JUnit 5, Mockito, `@WebMvcTest` + MockMvc |
| Local infra | Docker Compose (Postgres) |

## Architecture

Standard layered architecture, each layer depends only on the one directly beneath it:

```
controller  -> HTTP concerns only: status codes, routing, request/response shape
service     -> business rules, orchestration, transactions
repository  -> Spring Data JPA, plus hand-written JPQL for overlap/aggregate queries
entity      -> JPA-mapped domain model
dto         -> API request/response contracts, decoupled from the entity shape
mapper      -> entity ↔ dto conversion
exception   -> custom exceptions + a single @RestControllerAdvice mapping them to HTTP
config      -> OpenAPI metadata
```

Two services intentionally split what might look like one: `FlightService` handles plain CRUD, while `FlightAssignmentService` owns every cross-entity operation (assigning aircraft/pilots/crew, completing/cancelling a flight), keeping the constructor dependency count and cognitive scope of each class manageable.

## Domain model

```
Pilot ──1:N-> Availability            (unavailability windows; backs the "pilot can't be assigned if unavailable" rule)
CrewMember ──1:N-> WorkHours          (one row per completed flight worked; backs the weekly hour cap)

Flight ──N:1-> Aircraft
Flight ──1:N-> FlightPilotAssignment ──N:1-> Pilot
Flight ──1:N-> FlightCrewAssignment  ──N:1-> CrewMember
```

`FlightPilotAssignment`/`FlightCrewAssignment` are explicit join entities (not `@ManyToMany`) so each assignment can carry its own role and timestamp, the standard pattern for a many-to-many relationship that needs metadata on the relationship itself.

## Business rules

- A pilot cannot be assigned to a flight while marked unavailable, or while already assigned to another overlapping flight.
- A crew member cannot be assigned if it would push their logged hours over the configured weekly limit (`crew.max-weekly-hours`, default 40 over a rolling 7 days), or if already assigned to an overlapping flight.
- An aircraft cannot be assigned to two overlapping flights.
- A flight's arrival time must be after its departure time (enforced via a cross-field `@AssertTrue` validator).
- Flights follow a small state machine: `SCHEDULED → COMPLETED` or `SCHEDULED → CANCELLED`. Completing a flight requires an assigned aircraft and at least one assigned pilot; both transitions are blocked once a flight has left `SCHEDULED`.
- Completing a flight credits every assigned pilot's cumulative flight hours and logs a `WorkHours` record for every assigned crew member.

## API overview

Full interactive documentation is served by Swagger UI once the app is running — see below. Broad shape:

| Resource | Endpoints |
|---|---|
| `/api/pilots` | CRUD, `/available`, `/{id}/statistics` |
| `/api/crew-members` | CRUD, `/available` |
| `/api/aircraft` | CRUD, `/available` |
| `/api/flights` | CRUD, `/{id}/aircraft/{aircraftId}`, `/{id}/pilots/{pilotId}`, `/{id}/crew/{crewMemberId}`, `/{id}/complete`, `/{id}/cancel` |

Every error response follows a consistent shape (timestamp, status, error, message, path, and field-level detail for validation failures), produced by a single global exception handler rather than per-endpoint error handling.

## Running locally

**Prerequisites:** Java 21, Maven, Docker.

```bash
# 1. Start Postgres
docker compose up -d

# 2. Run the app
mvn spring-boot:run
```

The API is available at `http://localhost:8080`, Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Running tests

```bash
mvn test
```

112 tests, no database required, service-layer logic is verified with pure Mockito unit tests (`@ExtendWith(MockitoExtension.class)`), and the HTTP layer (routing, JSON, validation, error mapping) is verified separately with Spring's `@WebMvcTest` slice + MockMvc, with the service layer mocked out in both cases. The full suite runs in a few seconds.

## Validation

Beyond the automated test suite, the application was run end-to-end against a real PostgreSQL instance (via Docker Compose) and exercised manually through Swagger UI / `curl` — Hibernate schema generation, every CRUD endpoint, and every business rule (aircraft/pilot/crew double-booking, pilot unavailability, the crew weekly-hour cap at its exact boundary, flight completion crediting pilot hours and logging `WorkHours`, and cancellation freeing up an aircraft) were confirmed against live data rather than mocks.

That pass caught two real issues neither the unit nor slice tests could have, since both mock away the exact thing that broke:

- **A PostgreSQL port conflict.** A local Homebrew Postgres install was already bound to `127.0.0.1:5432`, which macOS resolves ahead of Docker's `0.0.0.0:5432` for connections to `localhost`, the app was silently connecting to the wrong database entirely. Fixed by moving the Docker container to host port `5433`.
- **A routing bug in the global exception handler.** Any URL with no matching route (a typo, or a path for a feature that doesn't exist, like `/actuator/health`) was caught by the catch-all `@ExceptionHandler(Exception.class)` and misreported as `500` instead of `404`, pre-empting Spring's own correct default handling. Fixed with an explicit handler for `NoResourceFoundException`, plus a regression test.

## Notable design decisions

- **DTOs are never entities.** Every endpoint takes/returns a purpose-built request/response DTO — separate `Create`/`Update`/`Response` shapes per entity, so server-owned fields (status defaults, computed statistics) can never be set directly by a client, and the API contract doesn't leak the database schema.
- **`FetchType.LAZY` set explicitly on every `@ManyToOne`**, since JPA's own default (`EAGER`) silently over-fetches.
- **Overlap checks reuse the same interval-overlap SQL pattern** (`existing.start < new.end AND existing.end > new.start`) across aircraft double-booking, pilot double-booking, and pilot unavailability, recognized as the same abstract problem applied to three different resources, rather than three different implementations.
- **The "available X" endpoints push filtering into SQL** (correlated `NOT EXISTS` / scalar subqueries) since the candidate set is the whole fleet/roster, while **per-flight side effects (`completeFlight`) filter in Java** since the data volume per flight is tiny — the same judgment applied at opposite scales.

## Possible next steps

Things deliberately left out of scope for this version, worth naming rather than leaving implicit:

- Schema migrations via Flyway/Liquibase instead of `ddl-auto: update`.
- Authentication/authorization (currently a fully open API).
- Optimistic locking (`@Version`) on `Flight` to close the race-condition window on concurrent completion/cancellation.
- Pagination on the list endpoints.
