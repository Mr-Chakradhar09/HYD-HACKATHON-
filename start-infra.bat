@echo off
echo Starting Infrastructure Services...
echo.

docker compose -f "%~dp0docker-compose.yml" up -d mysql zookeeper kafka

echo Starting Eureka Server...
start "Eureka Server" cmd /c "cd /d "%~dp0eureka-server" && mvn spring-boot:run -DskipTests"

timeout /t 15 /nobreak >nul

echo Starting Config Server...
start "Config Server" cmd /c "cd /d "%~dp0config-server" && mvn spring-boot:run -DskipTests"

echo.
echo Infrastructure starting up...
echo Eureka:  http://localhost:8761
