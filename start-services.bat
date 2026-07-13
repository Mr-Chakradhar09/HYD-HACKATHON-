@echo off
echo Starting All Microservices...
echo.

for %%s in (
    "auth-service"
    "warehouse-service"
    "product-service"
    "inventory-service"
    "replenishment-service"
    "reporting-service"
    "notification-service"
    "admin-server"
) do (
    echo Starting %%~s...
    start "%%~s" cmd /c "cd /d "%~dp0%%~s" && mvn spring-boot:run -DskipTests"
    timeout /t 8 /nobreak >nul
)

timeout /t 5 /nobreak >nul

echo Starting API Gateway...
start "API Gateway" cmd /c "cd /d "%~dp0api-gateway" && mvn spring-boot:run -DskipTests"

timeout /t 3 /nobreak >nul

echo Starting Frontend...
start "Frontend" cmd /c "cd /d "%~dp0inventory-ui" && npm run dev"

echo.
echo ============================================
echo   All services started!
echo   Gateway:  http://localhost:8080
echo   Frontend: http://localhost:3000
echo ============================================
