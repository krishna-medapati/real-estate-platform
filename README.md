# PrimeHaven — Enterprise Real Estate Listings & Property Inspection Scheduling Platform

Spring Boot microservices (SOA) + a lightweight React frontend, built against the
PS032 brief: property listings, visit scheduling, inquiry handling, JWT auth,
Eureka service discovery, and API Gateway routing.

## Architecture

```
                         ┌───────────────┐
                         │  eureka-server │  :9000  (service registry)
                         └───────▲────────┘
                                 │ registers
        ┌────────────────┬──────┴──────┬────────────────┐
        │                │             │                │
   auth-service    property-service  visit-service  inquiry-service
      :8011             :8013           :8014           :8012
   (auth_db)         (property_db)    (visit_db)      (inquiry_db)
        ▲                ▲──────────────┘  ▲               │
        │                │  (RestTemplate, │               │
        │                │  Eureka lookup) └───────────────┘
        │                                  Visit → Property, Inquiry → Property → Visit
        │
   ┌────┴─────┐
   │ api-gateway │ :8000  (routes /api/auth, /api/properties, /api/visits, /api/inquiries)
   └────┬─────┘
        │
   frontend/index.html  (open directly in a browser, talks to :8000)
```

Each service is an **independent** Spring Boot application with its **own PostgreSQL
database**, as requested — nothing is shared except the JWT signing secret (same
string hardcoded in `JwtUtil` in every service, so a token minted by `auth-service`
validates everywhere else).

- **JWT auth**: `auth-service` issues tokens with a `role` claim (`CLIENT` or `AGENT`).
  Every other service validates that token itself via a small `JwtAuthFilter` — the
  gateway just routes traffic, it doesn't terminate auth.
- **Eureka**: all five business services register with `eureka-server`. The gateway
  and the inter-service calls (`visit-service → property-service`,
  `inquiry-service → property-service → visit-service`) resolve targets by Eureka
  service name (`PROPERTY-SERVICE`, `VISIT-SERVICE`) via a `@LoadBalanced RestTemplate`,
  not hardcoded URLs.
- **API Gateway**: Spring Cloud Gateway routes `/api/auth/**`, `/api/properties/**`,
  `/api/visits/**`, `/api/inquiries/**` to the matching service by Eureka name.

## 1. Database setup (pgAdmin)

You're using PostgreSQL locally via pgAdmin. Open the **Query Tool** on the default
`postgres` database and run `database/create_databases.sql`, which creates:

```
auth_db, property_db, visit_db, inquiry_db
```

Each service's `application.yml` is preconfigured with:
```
username: postgres
password: Vanitas@41
```
**Note:** you said your pgAdmin username is "postgre" — PostgreSQL's default
superuser account is normally named `postgres` (with an `s`), so I used that.
If your actual role really is named `postgre`, change the `username:` line in
all four `application.yml` files (`auth-service`, `property-service`,
`visit-service`, `inquiry-service`) to match.

Tables are auto-created by Hibernate (`ddl-auto: update`) the first time each
service starts — you don't need to write `CREATE TABLE` statements yourself.

## 2. Running the backend

Each folder is a standalone Maven project. Open each in IntelliJ (or run from a
terminal with `mvn spring-boot:run` inside the folder) **in this order**, waiting
~10-15s between each so Eureka registration completes:

1. `eureka-server` — visit `http://localhost:9000` to confirm it's up
2. `auth-service`
3. `property-service`
4. `visit-service`
5. `inquiry-service`
6. `api-gateway`

Once everything is registered, `http://localhost:9000` should list all five
services (AUTH-SERVICE, PROPERTY-SERVICE, VISIT-SERVICE, INQUIRY-SERVICE,
API-GATEWAY).

## 3. Running the frontend

`frontend/index.html` is a single self-contained file (React + Babel loaded from
CDN, no build step). Just open it in a browser once the backend is running —
it talks to the gateway at `http://localhost:8000`. If you serve it from
somewhere other than a plain `file://` open, edit the `GATEWAY_URL` constant
near the top of the `<script>` block.

Flow to try it end-to-end:
1. Register two accounts: one as **Agent**, one as **Buyer/Client**.
2. Sign in as the agent → add a property listing.
3. Sign in as the client → browse listings, schedule a visit, send an inquiry.
4. Sign back in as the agent → see all visits and inquiries across clients.

## 4. API quick reference (via the gateway, `http://localhost:8000`)

| Method | Path | Role | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | — | `{username, password, role: CLIENT\|AGENT}` |
| POST | `/api/auth/login` | — | returns `{token, username, role}` |
| GET | `/api/properties` | any authenticated | list all |
| POST | `/api/properties` | AGENT | create |
| PUT | `/api/properties/{id}` | AGENT | update |
| PATCH | `/api/properties/{id}/status` | AGENT | `{status}` |
| DELETE | `/api/properties/{id}` | AGENT | |
| POST | `/api/visits` | CLIENT | `{propertyId, visitDate}` — rejects if property isn't `AVAILABLE` |
| GET | `/api/visits` | any | client sees own, agent sees all |
| DELETE | `/api/visits/{id}` | any authenticated | cancel |
| POST | `/api/inquiries` | CLIENT | `{propertyId, message}` — validates property, attaches related visits |
| GET | `/api/inquiries` | any | client sees own, agent sees all |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

## Project layout

```
real-estate-platform/
  database/create_databases.sql
  eureka-server/
  api-gateway/
  auth-service/
  property-service/
  visit-service/
  inquiry-service/
  frontend/index.html
```

## Notes & things to extend for the review

- Rubric item "Inter-service communication (Inquiry → Property → Visit)" is
  implemented in `InquiryService.createInquiry()`: it first calls
  `property-service` to validate the property, then calls `visit-service` to
  attach any scheduled visits for that property to the response.
- Load balancing is via Spring Cloud LoadBalancer (client-side, round-robin by
  default) — scale a service by starting a second instance on a different port;
  Eureka + the gateway will balance across both automatically.
- For production you'd move the hardcoded JWT secret and DB password into
  environment variables / a config server instead of `application.yml`.
