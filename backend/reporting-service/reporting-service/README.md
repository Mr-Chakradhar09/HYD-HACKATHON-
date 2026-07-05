# Employee Pulse Platform - Reporting Service (:8085)

The **Reporting Service** acts as the analytics and business intelligence engine of the AI-Powered Employee Pulse Survey Platform. It aggregates raw survey sentiment scores, location levels, and themes into dashboards, monthly snapshots, trends, and executive PDF/CSV reports.

## Architecture & Integration Flow

- **OpenFeign Clients**: Employs declarative REST clients targeting ports `8081-8084` to retrieve employee attributes, questionnaire themes, and AI sentiment scores.
- **Kafka Notifications**: Publishes structured JSON notification payloads on topic `notification-topic` to be consumed by `notification-service` (:8086).
- **Relational Aggregates**: Recalculates metrics automatically on data ingestion or monthly schedules into optimized snapshot tables to avoid querying heavy transactional writes during dashboard views.

---

## Technical Setup

### REST Endpoints

#### 1. Dashboard Controllers (`/api/reports/dashboard`)
- `GET /overview?month=yyyy-MM` - Retrieve overall organization pulse, sentiment statistics, concerns, and top cities.
- `GET /locations?month=yyyy-MM` - Morale scorecard for cities.
- `GET /themes?month=yyyy-MM` - Pulse index across dimensions (Work-Life Balance, Growth).
- `GET /departments?month=yyyy-MM` - Aggregated metrics for departments.

#### 2. Trends Controllers (`/api/trends`)
- `GET /organization` - Historical monthly trend timeline.
- `GET /location/{location}` - Trend line dataset for a target region/city.
- `GET /theme/{theme}` - Growth/decay trend of a concern area.

#### 3. AI Insights (`/api/reports/insights`)
- `GET /summary` - Narrated summary of organization health.
- `GET /top-concerns` - Hotspot concern dimensions.
- `GET /location` - Best vs worst location commentary.

#### 4. Exports (`/api/reports/export`)
- `GET /pdf?month=yyyy-MM` - Stream/download PDF dashboard.
- `GET /csv?month=yyyy-MM` - Stream/download data matrices.

#### 5. Administration & Ingestion
- `POST /api/reports/ingest` - Register feedback data to database and refresh dashboard aggregates dynamically.
- `POST /api/reports/aggregate?month=yyyy-MM` - Recalculate monthly records.

---

## How to Run

### Development Mode (In-Memory H2 DB)
The `dev` profile uses an in-memory database and boots without external configuration:
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```
Access H2 Console: `http://localhost:8085/h2-console` (JDBC URL: `jdbc:h2:mem:reportingdb`, User: `sa`, Pass: `password`).
Access Swagger UI Docs: `http://localhost:8085/swagger-ui/index.html`.

### Production Mode (MySQL & Kafka)
Start with `prod` profile:
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=prod
```
Or run the dockerized version:
```bash
docker build -t reporting-service .
docker run -p 8085:8085 -e SPRING_PROFILES_ACTIVE=prod reporting-service
```
