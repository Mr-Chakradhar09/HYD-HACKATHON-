# User Service

`user-service` is a production-ready Spring Boot microservice responsible for maintaining employee, HR, and Global HR profiles in the Employee Pulse Survey platform. It acts as the source of truth for all user profile information.

## Tech Stack
* **Java**: 21
* **Framework**: Spring Boot 3.5.16
* **Database**: MySQL 8.x
* **Persistence**: Spring Data JPA & Hibernate
* **Mapping**: ModelMapper 3.2.0
* **API Documentation**: Springdoc OpenAPI (Swagger UI) 2.8.5
* **Service Registry**: Eureka Client
* **HTTP Client**: OpenFeign
* **Build System**: Maven

---

## Key Features & Constraints

### Package Architecture
The project complies with clean coding guidelines and is structured as follows:
* `controller`: Handles REST endpoints and validates input requests.
* `service` & `service.impl`: Processes business rules, auditing, and authorization logic.
* `repository`: Interacts with MySQL through custom JPA query methods.
* `entity`: Houses the `User` database model.
* `dto.request` & `dto.response`: Data contract layer.
* `mapper`: Maps between entity and DTO using ModelMapper.
* `enums`: Stores strict types (`Role`, `Location`).
* `exception`: Houses global exception handlers and custom exceptions.
* `config`: Configures ModelMapper and OpenAPI Swagger endpoints.
* `common`: Defines standard envelopes for success and validation error payloads.

### Location List
We support the following global corporate locations:
* `CHENNAI`, `HYDERABAD`, `PUNE`, `BANGALORE`, `COLOMBO`, `SINGAPORE`, `MUMBAI`, `GURUGRAM`, `LONDON`, `NEW_YORK`, `TOKYO`, `SYDNEY`

### Onboarding Authorization Rules
The microservice restricts user profile creations, updates, and deactivations based on headers passed down from the API Gateway:
1. **GLOBAL_HR**: Authorized to onboard, update, and deactivate ANY user at any location.
2. **HR**: Authorized to onboard/update/deactivate ONLY users with the role `EMPLOYEE` belonging to their own location. They cannot update or deactivate Global HR or HR profiles.
3. **EMPLOYEE**: Read-only access to REST APIs. Cannot perform create, update, or deactivation operations.

---

## Security Architecture & API Gateway Integration

This service relies on a **Centralized Gateway Authentication** model. 

```
                                  +-------------------+
                                  |   Auth Service    |
                                  |  (Issues JWTs)    |
                                  +---------+---------+
                                            ^
                                            | 1. Validate JWT
                                            v
[ Client Request ] -------------> [ API Gateway ] --(Injects headers)--> [ User Service ]
 (Bearer Token)                    - Decodes JWT                           - X-User-Role
                                   - Strips client headers                 - X-User-Location
```

### Gateway Configurations (To Prevent Spoofing)
To ensure client applications cannot spoof their roles or locations by manually passing `X-User-Role` headers:
1. **Header Stripping**: The API Gateway must strip incoming `X-User-Role` and `X-User-Location` headers from all client requests.
2. **Header Injections**: The Gateway extracts claims from the validated JWT and inserts them as headers before forwarding the request to downstream services.

#### Example: Spring Cloud Gateway Routing Setup
Add the following gateway filter mapping in your API Gateway's configuration to securely propagate the claims:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            # 1. Strip any user-supplied auth headers to prevent spoofing
            - RemoveRequestHeader=X-User-Role
            - RemoveRequestHeader=X-User-Location
            # 2. Extract claims from the JWT and inject them safely
            # (Assuming JWT contains 'role' and 'location' claims)
            - TokenRelay=
            - AddRequestHeader=X-User-Role, #{principal.claims['role']}
            - AddRequestHeader=X-User-Location, #{principal.claims['location']}
```

---

## Configuration & Setup

### Requirements
* Java JDK 21 installed.
* Apache Maven 3.9+ installed.
* MySQL Server running on `localhost:3306` (or set the environment variables below).

### Database Config
Create a database named `pulse_survey_db` in MySQL or let the service auto-create it:
```sql
CREATE DATABASE IF NOT EXISTS pulse_survey_db;
```
If your MySQL credentials differ from `root` / `password`, override them by setting these environment variables:
* `SPRING_DATASOURCE_URL` (Default: `jdbc:mysql://localhost:3306/pulse_survey_db`)
* `SPRING_DATASOURCE_USERNAME` (Default: `root`)
* `SPRING_DATASOURCE_PASSWORD` (Default: `password`)

---

## How to Run

1. **Compile the microservice**:
   ```bash
   mvn clean compile
   ```

2. **Package the microservice**:
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the microservice**:
   ```bash
   java -jar target/user-service-0.0.1-SNAPSHOT.jar
   ```
   Or via Maven:
   ```bash
   mvn spring-boot:run
   ```

The application will launch on port **`8081`**.
* Swagger UI URL: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
* API Docs URL: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

---

## REST Endpoints Overview

| HTTP Method | Path | Required Headers | Description |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/users` | `X-User-Role`, `X-User-Location` | Onboard new user |
| **GET** | `/api/users/{id}` | None | Get user by DB ID |
| **GET** | `/api/users/employee/{employeeId}` | None | Get user by Employee ID |
| **GET** | `/api/users/email/{email}` | None | Get user by Email address |
| **GET** | `/api/users/location/{location}` | None | Get users filtered by Location |
| **GET** | `/api/users/role/{role}` | None | Get users filtered by Role |
| **PUT** | `/api/users/{id}` | `X-User-Role`, `X-User-Location` | Update user details |
| **PATCH** | `/api/users/{id}/deactivate` | `X-User-Role`, `X-User-Location` | Deactivate a user |

---

## Sample Payloads & Responses

### 1. Onboard User Request (POST `/api/users`)

**Request Payload:**
```json
{
  "employeeId": "EMP101",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@virtusa.com",
  "role": "EMPLOYEE",
  "location": "HYDERABAD",
  "department": "Engineering",
  "businessUnit": "Digital BU",
  "designation": "Associate Engineer"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User profile created successfully",
  "data": {
    "id": 5,
    "employeeId": "EMP101",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@virtusa.com",
    "role": "EMPLOYEE",
    "location": "HYDERABAD",
    "department": "Engineering",
    "businessUnit": "Digital BU",
    "designation": "Associate Engineer",
    "active": true,
    "createdAt": "2026-07-04T12:00:00",
    "updatedAt": "2026-07-04T12:00:00"
  },
  "timestamp": "2026-07-04T12:00:00.123456"
}
```

### 2. Validation Failure (400 Bad Request)

Returned when request attributes fail constraints (e.g. missing required fields or invalid email).

**Response:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "employeeId": "Employee ID is required",
    "email": "Invalid email format"
  },
  "timestamp": "2026-07-04T12:05:00.654321"
}
```

### 3. Authorization Violation (403 Forbidden)

Returned if an `HR` user tries to onboard an `HR` user, or an employee from another location, or if an `EMPLOYEE` caller tries to modify database records.

**Response:**
```json
{
  "success": false,
  "message": "HR users can only onboard employees belonging to their location (HYDERABAD). Requested location: LONDON",
  "data": null,
  "timestamp": "2026-07-04T12:10:00.987654"
}
```

---

## Postman Collection Template

Copy the JSON contents below and save it as `User_Service.postman_collection.json`. You can then import it into Postman directly.

```json
{
	"info": {
		"_postman_id": "8c4eb57a-bfde-4b1f-9988-34ebdf1a415a",
		"name": "User Service Microservice",
		"description": "Postman Collection for testing the Spring Boot user-service API with headers-based authorization simulation.",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
	},
	"item": [
		{
			"name": "Onboard User (Global HR)",
			"request": {
				"method": "POST",
				"header": [
					{
						"key": "X-User-Role",
						"value": "GLOBAL_HR",
						"type": "text"
					},
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\n  \"employeeId\": \"EMP201\",\n  \"firstName\": \"Alex\",\n  \"lastName\": \"Hales\",\n  \"email\": \"alex.hales@virtusa.com\",\n  \"role\": \"HR\",\n  \"location\": \"LONDON\",\n  \"department\": \"Human Resources\",\n  \"businessUnit\": \"UK BU\",\n  \"designation\": \"HR Specialist\"\n}"
				},
				"url": {
					"raw": "http://localhost:8081/api/users",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users"
					]
				}
			},
			"response": []
		},
		{
			"name": "Onboard User (HR Same Location - Hyderabad)",
			"request": {
				"method": "POST",
				"header": [
					{
						"key": "X-User-Role",
						"value": "HR",
						"type": "text"
					},
					{
						"key": "X-User-Location",
						"value": "HYDERABAD",
						"type": "text"
					},
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\n  \"employeeId\": \"EMP202\",\n  \"firstName\": \"Sunil\",\n  \"lastName\": \"Gavaskar\",\n  \"email\": \"sunil.gavaskar@virtusa.com\",\n  \"role\": \"EMPLOYEE\",\n  \"location\": \"HYDERABAD\",\n  \"department\": \"Engineering\",\n  \"businessUnit\": \"Digital BU\",\n  \"designation\": \"Senior Analyst\"\n}"
				},
				"url": {
					"raw": "http://localhost:8081/api/users",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users"
					]
				}
			},
			"response": []
		},
		{
			"name": "Onboard User (HR Different Location - Forbidden)",
			"request": {
				"method": "POST",
				"header": [
					{
						"key": "X-User-Role",
						"value": "HR",
						"type": "text"
					},
					{
						"key": "X-User-Location",
						"value": "HYDERABAD",
						"type": "text"
					},
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\n  \"employeeId\": \"EMP203\",\n  \"firstName\": \"Marcus\",\n  \"lastName\": \"Stoinis\",\n  \"email\": \"marcus.stoinis@virtusa.com\",\n  \"role\": \"EMPLOYEE\",\n  \"location\": \"LONDON\",\n  \"department\": \"Engineering\",\n  \"businessUnit\": \"UK BU\",\n  \"designation\": \"Tech Lead\"\n}"
				},
				"url": {
					"raw": "http://localhost:8081/api/users",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get User by DB ID",
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "http://localhost:8081/api/users/1",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"1"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get User by Employee ID",
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "http://localhost:8081/api/users/employee/EMP001",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"employee",
						"EMP001"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get User by Email",
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "http://localhost:8081/api/users/email/alice.smith@virtusa.com",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"email",
						"alice.smith@virtusa.com"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get Users by Location",
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "http://localhost:8081/api/users/location/HYDERABAD",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"location",
						"HYDERABAD"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get Users by Role",
			"request": {
				"method": "GET",
				"header": [],
				"url": {
					"raw": "http://localhost:8081/api/users/role/EMPLOYEE",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"role",
						"EMPLOYEE"
					]
				}
			},
			"response": []
		},
		{
			"name": "Update User Profile (HR)",
			"request": {
				"method": "PUT",
				"header": [
					{
						"key": "X-User-Role",
						"value": "HR",
						"type": "text"
					},
					{
						"key": "X-User-Location",
						"value": "HYDERABAD",
						"type": "text"
					},
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\n  \"firstName\": \"Charlie\",\n  \"lastName\": \"Brown-updated\",\n  \"role\": \"EMPLOYEE\",\n  \"location\": \"HYDERABAD\",\n  \"department\": \"Engineering\",\n  \"businessUnit\": \"Digital BU (Hybrid)\",\n  \"designation\": \"Staff Software Engineer\"\n}"
				},
				"url": {
					"raw": "http://localhost:8081/api/users/3",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"3"
					]
				}
			},
			"response": []
		},
		{
			"name": "Deactivate User profile",
			"request": {
				"method": "PATCH",
				"header": [
					{
						"key": "X-User-Role",
						"value": "GLOBAL_HR",
						"type": "text"
					}
				],
				"url": {
					"raw": "http://localhost:8081/api/users/4/deactivate",
					"protocol": "http",
					"host": [
						"localhost"
					],
					"port": "8081",
					"path": [
						"api",
						"users",
						"4",
						"deactivate"
					]
				}
			},
			"response": []
		}
	]
}
```
