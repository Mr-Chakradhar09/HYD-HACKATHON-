# Survey Service Microservice

Welcome to the **Survey Service** microservice, the core business engine of the **AI-Powered Employee Pulse Survey Platform**. It is designed with Spring Boot 3.4.x, Java 21, clean architecture principles, and Domain-Driven Design concepts.

---

## 🛠 Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.1 / Spring Cloud 2024.0.0
- **Database**: MySQL 8+ (with JPA and Hibernate ORM)
- **Messaging**: Apache Kafka (for decoupled notifications to the Notification Service)
- **Integration**: OpenFeign (for Auth and AI clients)
- **APIs**: REST with Swagger/OpenAPI documentation
- **Security**: Spring Security 6 with JWT state parsing
- **Testing**: JUnit 5, Mockito, and Spring Integration Tests (H2 database)
- **Containerization**: Docker-ready multi-stage setup

---

## 🏢 Architecture & Security Design

The Survey Service operates in a multi-tenant location-bound context:
1. **`GLOBAL_HR`**: Can view all survey structures, reports, and questions globally. They manage the global question bank and question versioning.
2. **`HR`**: Operates under location tenancy constraints. They can only view surveys, responses, reports, and draft questions created for their *assigned location*. Location claims are parsed dynamically from incoming JWTs and verified programmatically.
3. **`EMPLOYEE`**: Can fetch active surveys for their location, check their submission status, and post responses.

---

## 💾 Database Schema

The microservice owns the following MySQL tables:
- **`questions`**: Stores global question bank items. Handles soft deactivations and versioning.
- **`surveys`**: Tracks title, location, target month/year, bank version, and publishing state.
- **`survey_questions`**: Junction table mapping questions to surveys in a sorted order.
- **`survey_responses`**: Tracks user survey completions (preventing duplicates).
- **`survey_answers`**: Holds rating values and comment texts left for survey questions.
- **`draft_questions`**: Houses generated AI suggestions awaiting HR review.
- **`question_versions`**: Historical log records of question bank version updates.

---

## 🚀 Kafka Notifications

When critical survey lifecycle events occur, the service broadcasts events to the `survey-notifications` topic:
1. **`SURVEY_PUBLISHED`**: Notifies all employees in the survey's target location.
2. **`SURVEY_CLOSED`**: Notifies location HR that responses are finalized.
3. **`DRAFT_GENERATED`**: Signals that AI suggested questions are ready for HR's approval.

*Fallback*: If the Kafka broker is down or disabled, the service automatically routes notifications through the **Notification HTTP Feign client** to ensure zero-loss delivery.

---

## 🌐 REST Endpoints

### Question Bank APIs
- `POST /api/questions` - Add a question (GLOBAL_HR)
- `GET /api/questions` - View active questions (paginated) or version list (GLOBAL_HR/HR)
- `GET /api/questions/{id}` - View question details (GLOBAL_HR/HR)
- `PUT /api/questions/{id}` - Edit question details (GLOBAL_HR)
- `DELETE /api/questions/{id}` - Soft-delete a question (GLOBAL_HR)

### Survey Management APIs
- `POST /api/surveys` - Create a monthly survey (HR/GLOBAL_HR)
- `GET /api/surveys` - List surveys (location filtered for HR)
- `GET /api/surveys/{id}` - View survey details
- `PUT /api/surveys/{id}` - Edit draft survey details (HR/GLOBAL_HR)
- `DELETE /api/surveys/{id}` - Delete draft survey (HR/GLOBAL_HR)
- `POST /api/surveys/{id}/questions` - Manually select questions for draft survey (HR/GLOBAL_HR)
- `POST /api/surveys/{id}/publish` - Publish a draft survey (HR/GLOBAL_HR)
- `POST /api/surveys/{id}/close` - Close a published survey (HR/GLOBAL_HR)
- `GET /api/surveys/active` - Fetch current active survey for employee location (EMPLOYEE)

### Response & Report APIs
- `POST /api/responses` - Submit survey answers (EMPLOYEE)
- `GET /api/responses/{surveyId}` - Generate pulse metrics report (HR/GLOBAL_HR)

### AI & Draft Approval APIs
- `POST /api/surveys/{surveyId}/generate-ai-questions` - Trigger AI analysis (HR/GLOBAL_HR)
- `GET /api/draft-questions` - View generated draft suggestions (HR/GLOBAL_HR)
- `PUT /api/draft-questions/{id}/edit` - Edit draft question parameters (HR/GLOBAL_HR)
- `PUT /api/draft-questions/{id}/approve` - Approve draft (HR/GLOBAL_HR; increments bank version)
- `PUT /api/draft-questions/{id}/reject` - Reject draft question (HR/GLOBAL_HR)

---

## 🏃 Running the Application

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- MySQL 8 Instance (running database named `survey_db`)
- Optional: Apache Kafka (localhost:9092)

### Steps
1. Clone / open the workspace.
2. Build with maven:
   ```bash
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
4. Access Swagger documentation at:
   - http://localhost:8082/swagger-ui.html

---

## 🧪 Running Tests

Run the test suite including unit tests and Spring ApplicationContext checks:
```bash
mvn test
```
