# Auth Service - Employee Pulse Survey Platform

The `auth-service` is a microservice responsible for authentication, credential creation, password updates, and JWT generation/validation for the Employee Pulse Survey platform.

## Technology Stack

* **Java 21**
* **Spring Boot 3.4.1** (Production-grade stable version)
* **Spring Security** (Stateless authentication)
* **JWT (JSON Web Token)**
* **MySQL Database**
* **Spring Data JPA**
* **OpenFeign** (For inter-service communications)
* **Eureka Client** (For service discovery)
* **Lombok**
* **Maven**

---

## Security Architecture & API Gateway Integration

Authentication and authorization checks are designed to align with microservice security patterns:

### 1. Security Topology
* **Centralization**: Authentication is centralized at the API Gateway layer. External clients send requests containing bearer JWT tokens to the Gateway.
* **Internal Routing**: The API Gateway intercepts the request, validates the token, extracts the claims, and maps them to HTTP headers.
* **Downstream Services**: Services like `user-service` and `survey-service` read these headers rather than extracting JWTs directly. This prevents duplicate validation code, optimizes performance, and keeps the microservices highly testable.

```
[Public Client] 
     │  (HTTPS with Bearer JWT)
     ▼
[API Gateway]  ◄──►  [Auth Service (validate API)]
     │
     │  1. Strips incoming 'X-User-*' headers (Prevents Spoofing)
     │  2. Injects validated claims:
     │     - X-User-Role = HR
     │     - X-User-Location = US
     ▼
[User/Survey Service] (Extracts headers, applies business-layer RBAC)
```

### 2. Preventing Spoofing
To ensure client integrity, the Gateway **must strip** all client-supplied headers resembling internal parameters (e.g., `X-User-Role`, `X-User-Location`) before forwarding the request to downstream services.

### 3. Gateway Routing Configuration
Below is an example Spring Cloud Gateway routing config illustrating how claims are safely extracted and mapped:

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
            # Prevent header spoofing from incoming requests
            - RemoveRequestHeader=X-User-Role
            - RemoveRequestHeader=X-User-Location
            # Safe claim mapping filter (custom or via JwtClaimsHeaderFilter)
            - InjectClaimsToHeaders=role:X-User-Role,location:X-User-Location
```

---

## Directory Structure

```
auth-service
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── virtusa
│   │   │           └── authservice
│   │   │               ├── client         # Feign client for User Service
│   │   │               ├── common         # Common envelope responses
│   │   │               ├── config         # Feign, Swagger, PasswordEncoder configuration
│   │   │               ├── controller     # REST API Controllers
│   │   │               ├── dto            # Request and Response payloads
│   │   │               ├── entity         # JPA Entities
│   │   │               ├── exception      # Custom exception definitions and Global Handler
│   │   │               ├── repository     # Spring Data JPA Repositories
│   │   │               ├── security       # JwtService, Filters, UserDetails configuration
│   │   │               ├── util           # Header utility helpers
│   │   │               └── AuthServiceApplication.java
│   │   └── resources
│   │         ├── application.yml          # Spring configuration settings
│   │         └── data.sql                 # Seeds database with initial accounts
└── pom.xml                                # Maven build specifications
```

---

## Database Seeding (Default Accounts)

The database is seeded on startup with the following accounts. The password for the first three is `password`, and for the fourth (`EMP101`) is `tempPassword123`.

| Employee ID | Email | Role | Location | Enabled |
| :--- | :--- | :--- | :--- | :--- |
| `EMP001` | `globalhr@virtusa.com` | `GLOBAL_HR` | `Global` | `true` |
| `EMP002` | `hr.us@virtusa.com` | `HR` | `US` | `true` |
| `EMP003` | `employee.us@virtusa.com` | `EMPLOYEE` | `US` | `true` |
| `EMP101` | `newemployee@virtusa.com` | `EMPLOYEE` | `UK` | `true` |

---

## API Documentation & Sample Payloads

### 1. User Login
* **URL**: `POST /api/auth/login`
* **Public**: Yes
* **Request Payload**:
  ```json
  {
    "email": "hr.us@virtusa.com",
    "password": "password"
  }
  ```
* **Response Payload**:
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbXBsb3llZUlkIjoiRU1QMDAyIiwiZW1haWwiOiJoci51c0B2aXJ0dXNhLmNvbSIsInJvbGUiOiJIUiIsImxvY2F0aW9uIjoiVVMiLCJzdWIiOiJoci51c0B2aXJ0dXNhLmNvbSIsImlhdCI6MTc4MTIzNDU2NywiZXhwIjoxNzgxMzIwOTY3fQ...",
      "employeeId": "EMP002",
      "email": "hr.us@virtusa.com",
      "role": "HR",
      "location": "US"
    },
    "timestamp": "2026-07-04T12:45:00"
  }
  ```

### 2. Validate Token (Used by Gateway)
* **URL**: `POST /api/auth/validate`
* **Public**: Yes
* **Request Payload**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbXBsb3llZUlkIjoiRU1QMDAyIiwiZW1haWwi..."
  }
  ```
* **Response Payload**:
  ```json
  {
    "success": true,
    "message": "Token is valid",
    "data": {
      "valid": true,
      "employeeId": "EMP002",
      "email": "hr.us@virtusa.com",
      "role": "HR",
      "location": "US"
    },
    "timestamp": "2026-07-04T12:46:12"
  }
  ```

### 3. Get Current User Details
* **URL**: `GET /api/auth/me`
* **Headers**: `Authorization: Bearer <token>`
* **Response Payload**:
  ```json
  {
    "success": true,
    "message": "Current user retrieved successfully",
    "data": {
      "employeeId": "EMP002",
      "email": "hr.us@virtusa.com",
      "role": "HR",
      "location": "US"
    },
    "timestamp": "2026-07-04T12:47:00"
  }
  ```

### 4. Change Password
* **URL**: `POST /api/auth/change-password`
* **Headers**: `Authorization: Bearer <token>`
* **Request Payload**:
  ```json
  {
    "oldPassword": "password",
    "newPassword": "newSecurePassword123"
  }
  ```
* **Response Payload**:
  ```json
  {
    "success": true,
    "message": "Password changed successfully",
    "data": null,
    "timestamp": "2026-07-04T12:48:30"
  }
  ```

### 5. Create Credentials (Internal endpoint used by User Service)
* **URL**: `POST /internal/auth/create-credentials`
* **Public**: Yes (Secured at network level / internal Gateway routing)
* **Request Payload**:
  ```json
  {
    "employeeId": "EMP004",
    "email": "new.employee@virtusa.com",
    "password": "tempPassword123",
    "role": "EMPLOYEE",
    "location": "UK"
  }
  ```
* **Response Payload**:
  ```json
  {
    "success": true,
    "message": "Credentials created successfully",
    "data": null,
    "timestamp": "2026-07-04T12:49:15"
  }
  ```

---

## Postman Collection Example

Import the JSON snippet below into Postman to load test requests:

```json
{
  "info": {
    "_postman_id": "893c5d6e-9ab7-47ee-881c-cb7676767676",
    "name": "Auth Service API",
    "description": "Requests targeting Auth Service endpoints.",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"hr.us@virtusa.com\",\n  \"password\": \"password\"\n}"
        },
        "url": {
          "raw": "http://localhost:8085/api/auth/login",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8085",
          "path": [
            "api",
            "auth",
            "login"
          ]
        }
      },
      "response": []
    },
    {
      "name": "Validate Token",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"token\": \"<JWT_TOKEN_HERE>\"\n}"
        },
        "url": {
          "raw": "http://localhost:8085/api/auth/validate",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8085",
          "path": [
            "api",
            "auth",
            "validate"
          ]
        }
      },
      "response": []
    },
    {
      "name": "Get Current User",
      "request": {
        "auth": {
          "type": "bearer",
          "bearer": [
            {
              "key": "token",
              "value": "<JWT_TOKEN_HERE>",
              "type": "string"
            }
          ]
        },
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8085/api/auth/me",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8085",
          "path": [
            "api",
            "auth",
            "me"
          ]
        }
      },
      "response": []
    },
    {
      "name": "Change Password",
      "request": {
        "auth": {
          "type": "bearer",
          "bearer": [
            {
              "key": "token",
              "value": "<JWT_TOKEN_HERE>",
              "type": "string"
            }
          ]
        },
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"oldPassword\": \"password\",\n  \"newPassword\": \"newSecurePassword123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8085/api/auth/change-password",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8085",
          "path": [
            "api",
            "auth",
            "change-password"
          ]
        }
      },
      "response": []
    },
    {
      "name": "Create Credentials (Internal)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"employeeId\": \"EMP004\",\n  \"email\": \"new.employee@virtusa.com\",\n  \"password\": \"tempPassword123\",\n  \"role\": \"EMPLOYEE\",\n  \"location\": \"UK\"\n}"
        },
        "url": {
          "raw": "http://localhost:8085/internal/auth/create-credentials",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "8085",
          "path": [
            "internal",
            "auth",
            "create-credentials"
          ]
        }
      },
      "response": []
    }
  ]
}
```
