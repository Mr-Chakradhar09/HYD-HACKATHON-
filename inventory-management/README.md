# Event-Driven Microservices Inventory Management System

A state-of-the-art, enterprise-grade **Inventory Management System** designed with a distributed, event-driven microservices architecture. The system supports multi-warehouse inventory tracking, product catalog management, role-based access control, automated purchase replenishment requests, reporting dashboards, and real-time notification alerts.

---

## 🏗️ System Architecture

The application is structured into modular microservices communicating synchronously via REST and asynchronously via Apache Kafka:

*   **API Gateway** (Port `8080`): The single entry point for all client requests; handles routing and JWT authentication checks.
*   **Eureka Server** (Port `8761`): Netflix Eureka service registry providing discovery and load balancing capabilities.
*   **Config Server** (Port `8888`): Spring Cloud Config Server managing centralized application properties across environments.
*   **Auth Service** (Port `8081`): Manages users, roles (`SYSTEM_ADMIN`, `WAREHOUSE_MANAGER`, `INVENTORY_MANAGER`, `PROCUREMENT_MANAGER`), and issues JWT access tokens.
*   **Product Service** (Port `8082`): Manages the global product catalog.
*   **Warehouse Service** (Port `8083`): Tracks warehouse entities and physical locations.
*   **Inventory Service** (Port `8084`): Tracks stock levels, processes inbound/outbound adjustments, and publishes stock transaction events.
*   **Replenishment Service** (Port `8085`): Automated & manual creation of purchase requests for low-stock items.
*   **Reporting Service** (Port `8086`): Aggregates real-time inventory statistics and movement metrics using Spring's fluent `RestClient`.
*   **Notification Service** (Port `8087`): Consumes inventory events from Kafka and dispatches automated alerts.
*   **Inventory UI** (Port `3000`): React & Vite frontend using Material-UI (MUI) components and Recharts.

---

## 🛠️ Technology Stack & Versions

*   **Backend**: Java 17 (LTS), Spring Boot 3.2.0, Spring Cloud 2023.0.0
*   **Database**: MySQL 8.0 (persistent storage)
*   **Message Broker**: Apache Kafka & ZooKeeper (event-driven broker)
*   **Security**: Stateless JWT (JSON Web Tokens)
*   **Frontend**: React 18.2, Vite 5.0, Material-UI (MUI) 9.2.0, Recharts 2.10.3, React Router DOM 6.20.0
*   **Containerization**: Docker & Docker Compose 3.8

---

## ✨ Features

*   **Event-Driven Communication**: Real-time notifications for warehouse-to-warehouse transfers and low-stock replenishment events powered by Apache Kafka.
*   **Modern Java 17 Features**: Optimized codebase utilizing standard Stream API features (`Stream.toList()`), clean class records, and factory collection instantiations.
*   **Spring Boot 3.2 RestClient**: Fluent synchronous HTTP calls replacing legacy RestTemplates.
*   **Stateless Security**: Role-based access control checking credentials at the API Gateway level.
*   **Responsive Frontend Dashboard**: Analytical charts displaying inbound/outbound stock metrics, product ratios per warehouse, and current purchase request statuses.

---

## 🚀 Getting Started

### Prerequisites
*   Java Development Kit (JDK) 17+
*   Maven 3.8+
*   Docker Desktop / Docker Engine
*   Node.js & npm (for running the UI locally)

### Setup & Run Infrastructure
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/YOUR_USERNAME/inventory-management-system-microservices.git
    cd inventory-management-system-microservices
    ```
2.  **Start Third-Party Services**:
    Launch the MySQL and Kafka containers:
    ```bash
    start-infra.bat
    # Or manually via docker compose:
    docker compose up -d mysql zookeeper kafka
    ```

### Run Spring Boot Applications & UI
You can start all microservices sequentially using the preconfigured startup scripts:
*   **Launch Services**:
    ```bash
    run-all-services.bat
    ```
    *This script spins up Eureka, Config Server, API Gateway, core business services, and initiates the React frontend.*

Once all services start, you can access the system portals:
*   **Eureka Registry**: `http://localhost:8761`
*   **API Gateway entry**: `http://localhost:8080`
*   **React Frontend Dashboard**: `http://localhost:3000`

---

## 🔑 Demo Credentials

| Username | Password | Role |
| :--- | :--- | :--- |
| `admin@inventory.com` | `admin123` | `SYSTEM_ADMIN` |
| `warehouse@inventory.com` | `warehouse123` | `WAREHOUSE_MANAGER` |
| `manager@inventory.com` | `manager123` | `INVENTORY_MANAGER` |
| `procurement@inventory.com` | `procurement123` | `PROCUREMENT_MANAGER` |
