@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Running All Service Tests
echo ============================================
echo.

set "ROOT_DIR=%~dp0"
set "PASS=0"
set "FAIL=0"
set "FAILED_SERVICES="

for %%s in (
    "auth-service"
    "warehouse-service"
    "product-service"
    "inventory-service"
    "replenishment-service"
    "reporting-service"
    "notification-service"
    "api-gateway"
) do (
    echo ------------------------------------------
    echo Testing: %%s
    echo ------------------------------------------
    cd /d "%ROOT_DIR%%%~s"
    call mvn test
    
    if !ERRORLEVEL! equ 0 (
        set /a PASS+=1
        echo [PASS] %%~s
    ) else (
        set /a FAIL+=1
        set "FAILED_SERVICES=!FAILED_SERVICES! %%~s"
        echo [FAIL] %%~s
    )
    echo.
)

echo ============================================
echo   Test Summary
echo ============================================
echo Passed: %PASS%
echo Failed: %FAIL%
if not "%FAILED_SERVICES%"=="" (
    echo Failed Services:%FAILED_SERVICES%
)
echo ============================================

if %FAIL% gtr 0 (
    exit /b 1
)
exit /b 0
