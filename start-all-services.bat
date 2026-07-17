@echo off
echo ============================================
echo   Inventory Management - Start All Services
echo ============================================
echo.

echo Stopping any existing services...
for %%p in (8761 8888 8089 8080 8082 8083 8084 8085 8086 8087 8090) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%p ^| findstr LISTENING') do (
        echo   Killing process on port %%p (PID %%a)
        taskkill /F /PID %%a >nul 2>&1
    )
)
timeout /t 3 /nobreak
echo.

echo [1/10] Starting Eureka Server on port 8761...
start "Eureka Server" cmd /k "cd /d %~dp0eureka-server && mvnw.cmd spring-boot:run"
timeout /t 20 /nobreak

echo [2/10] Starting Config Server on port 8888...
start "Config Server" cmd /k "cd /d %~dp0config-server && mvnw.cmd spring-boot:run"
timeout /t 15 /nobreak

echo [3/10] Starting Auth Service on port 8089...
start "Auth Service" cmd /k "cd /d %~dp0auth-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak

echo [4/10] Starting API Gateway on port 8080...
start "API Gateway" cmd /k "cd /d %~dp0api-gateway && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak

echo [5/10] Starting Product Service on port 8082...
start "Product Service" cmd /k "cd /d %~dp0product-service && mvnw.cmd spring-boot:run"
timeout /t 8 /nobreak

echo [6/10] Starting Warehouse Service on port 8083...
start "Warehouse Service" cmd /k "cd /d %~dp0warehouse-service && mvnw.cmd spring-boot:run"
timeout /t 8 /nobreak

echo [7/10] Starting Movement Service on port 8084...
start "Movement Service" cmd /k "cd /d %~dp0movement-service && mvnw.cmd spring-boot:run"
timeout /t 8 /nobreak

echo [8/10] Starting Inventory Service on port 8085...
start "Inventory Service" cmd /k "cd /d %~dp0inventory-service && mvnw.cmd spring-boot:run"
timeout /t 8 /nobreak

echo [9/10] Starting Replenishment Service on port 8086...
start "Replenishment Service" cmd /k "cd /d %~dp0replenishment-service && mvnw.cmd spring-boot:run"
timeout /t 8 /nobreak

echo [10/11] Starting Reporting Service on port 8087...
start "Reporting Service" cmd /k "cd /d %~dp0reporting-service && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak

echo [11/11] Starting Admin Server on port 8090...
start "Admin Server" cmd /k "cd /d %~dp0admin-server && mvnw.cmd spring-boot:run"
timeout /t 10 /nobreak

echo.
echo ============================================
echo   All services started!
echo.
echo   Eureka Dashboard: http://localhost:8761
echo   Config Server:    http://localhost:8888
echo   API Gateway:      http://localhost:8080
echo   Admin Server:     http://localhost:8090
echo.
echo   Services:
echo     Auth Service:          http://localhost:8089
echo     Product Service:       http://localhost:8082
echo     Warehouse Service:     http://localhost:8083
echo     Movement Service:      http://localhost:8084
echo     Inventory Service:     http://localhost:8085
echo     Replenishment Service: http://localhost:8086
echo     Reporting Service:     http://localhost:8087
echo.
echo   Prerequisites: MySQL (3306), Kafka (9092)
echo ============================================
pause
