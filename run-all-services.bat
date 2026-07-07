@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Inventory Management System - Startup
echo ============================================
echo.

set "ROOT_DIR=%~dp0"
set "MVNW_OPTS=-DskipTests"

REM ---- Check for Maven ----
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven not found. Please install Maven or use mvnw.
    exit /b 1
)

REM ---- Start MySQL via Docker ----
echo [1/7] Starting MySQL via Docker...
docker compose -f "%ROOT_DIR%docker-compose.yml" up -d mysql
if %ERRORLEVEL% neq 0 (
    echo [WARN] Docker not available or MySQL start failed. Ensure MySQL is running manually.
) else (
    echo [OK] MySQL started.
    timeout /t 10 /nobreak >nul
)

REM ---- Start Eureka Server ----
echo [2/7] Starting Eureka Server...
start "Eureka Server" cmd /c "cd /d "%ROOT_DIR%eureka-server" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 15 /nobreak >nul

REM ---- Start Config Server ----
echo [3/7] Starting Config Server...
start "Config Server" cmd /c "cd /d "%ROOT_DIR%config-server" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 10 /nobreak >nul

REM ---- Start Auth Service ----
echo [4/7] Starting Auth Service...
start "Auth Service" cmd /c "cd /d "%ROOT_DIR%auth-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 10 /nobreak >nul

REM ---- Start Product Service ----
echo [5/7] Starting Product Service...
start "Product Service" cmd /c "cd /d "%ROOT_DIR%product-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start Warehouse Service ----
echo [5/7] Starting Warehouse Service...
start "Warehouse Service" cmd /c "cd /d "%ROOT_DIR%warehouse-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start Inventory Service ----
echo [6/7] Starting Inventory Service...
start "Inventory Service" cmd /c "cd /d "%ROOT_DIR%inventory-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start Replenishment Service ----
echo [6/7] Starting Replenishment Service...
start "Replenishment Service" cmd /c "cd /d "%ROOT_DIR%replenishment-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start Reporting Service ----
echo [6/7] Starting Reporting Service...
start "Reporting Service" cmd /c "cd /d "%ROOT_DIR%reporting-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start Notification Service ----
echo [6/7] Starting Notification Service...
start "Notification Service" cmd /c "cd /d "%ROOT_DIR%notification-service" && mvn spring-boot:run %MVNW_OPTS%"
timeout /t 8 /nobreak >nul

REM ---- Start API Gateway ----
echo [7/7] Starting API Gateway...
start "API Gateway" cmd /c "cd /d "%ROOT_DIR%api-gateway" && mvn spring-boot:run %MVNW_OPTS%"

REM ---- Start Frontend ----
echo [6/7] Starting Frontend (Vite)...
start "Frontend" cmd /c "cd /d "%ROOT_DIR%inventory-ui" && npm run dev"

echo.
echo ============================================
echo   All services are starting up!
echo   Eureka:   http://localhost:8761
echo   Gateway:  http://localhost:8080
echo   Frontend: http://localhost:3000
echo ============================================
echo.
echo Press any key to stop all services...
pause >nul

echo.
echo Stopping all services...
taskkill /f /fi "WINDOWTITLE eq Eureka Server" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Config Server" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Auth Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Product Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Warehouse Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Inventory Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Replenishment Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Reporting Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Notification Service" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq API Gateway" >nul 2>nul
taskkill /f /fi "WINDOWTITLE eq Frontend" >nul 2>nul

echo All services stopped.
pause
