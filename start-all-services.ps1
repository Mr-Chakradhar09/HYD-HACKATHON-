# ============================================================
# Inventory Management - Microservices Startup Script
# ============================================================
# Starts all 11 services in dependency order with health checks.
# Prerequisites: MySQL (3306), Kafka (9092) must be running.
# ============================================================

param(
    [switch]$SkipInfrastructureCheck,
    [switch]$OnlyInfrastructure,
    [switch]$StopAll
)

$BASE_DIR = $PSScriptRoot
$LOG_DIR = Join-Path $BASE_DIR "logs"

# Service definitions: name, port, directory
$INFRA_SERVICES = @(
    @{ Name = "eureka-server";     Port = 8761; Dir = "eureka-server" },
    @{ Name = "config-server";     Port = 8888; Dir = "config-server" },
    @{ Name = "admin-server";      Port = 8090; Dir = "admin-server" }
)

$AUTH_GATEWAY_SERVICES = @(
    @{ Name = "auth-service";      Port = 8089; Dir = "auth-service" },
    @{ Name = "api-gateway";       Port = 8080; Dir = "api-gateway" }
)

$BUSINESS_SERVICES = @(
    @{ Name = "product-service";       Port = 8082; Dir = "product-service" },
    @{ Name = "warehouse-service";     Port = 8083; Dir = "warehouse-service" },
    @{ Name = "movement-service";      Port = 8084; Dir = "movement-service" },
    @{ Name = "inventory-service";     Port = 8085; Dir = "inventory-service" },
    @{ Name = "replenishment-service"; Port = 8086; Dir = "replenishment-service" },
    @{ Name = "reporting-service";     Port = 8087; Dir = "reporting-service" }
)

$ALL_SERVICES = $INFRA_SERVICES + $AUTH_GATEWAY_SERVICES + $BUSINESS_SERVICES

# ---- Helper Functions ----

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host ("=" * 60) -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host ("=" * 60) -ForegroundColor Cyan
}

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host ">>> $Message" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "    [OK] $Message" -ForegroundColor Green
}

function Write-Fail {
    param([string]$Message)
    Write-Host "    [FAIL] $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "    [INFO] $Message" -ForegroundColor Gray
}

function Test-Port {
    param([int]$Port)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $Port)
        $tcp.Close()
        return $true
    } catch {
        return $false
    }
}

function Test-HttpHealth {
    param([int]$Port, [string]$Path = "/actuator/health", [int]$TimeoutSec = 2)
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$Port$Path" -TimeoutSec $TimeoutSec -UseBasicParsing -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Wait-ForService {
    param(
        [string]$Name,
        [int]$Port,
        [int]$MaxWaitSec = 120,
        [int]$PollIntervalSec = 3
    )

    Write-Info "Waiting for $Name on port $Port (timeout ${MaxWaitSec}s)..."
    $elapsed = 0
    while ($elapsed -lt $MaxWaitSec) {
        if (Test-Port -Port $Port) {
            if (Test-HttpHealth -Port $Port) {
                Write-Success "$Name is ready (${elapsed}s)"
                return $true
            }
        }
        Start-Sleep -Seconds $PollIntervalSec
        $elapsed += $PollIntervalSec
        Write-Host "." -NoNewline -ForegroundColor DarkGray
    }
    Write-Host ""
    Write-Fail "$Name failed to start within ${MaxWaitSec}s"
    return $false
}

function Start-MavenService {
    param(
        [string]$Name,
        [string]$Dir,
        [int]$Port
    )

    $servicePath = Join-Path $BASE_DIR $Dir
    if (-not (Test-Path $servicePath)) {
        Write-Fail "Directory not found: $servicePath"
        return $false
    }

    $logFile = Join-Path $LOG_DIR "$Name.log"
    Write-Info "Starting $Name from $Dir (port $Port)..."
    Write-Info "Log: $logFile"

    # Start mvn spring-boot:run in a new window
    $startArgs = "/c cd /d `"$servicePath`" && mvn spring-boot:run > `"$logFile`" 2>&1"
    Start-Process cmd -ArgumentList $startArgs -WindowStyle Minimized

    return $true
}

function Stop-AllServices {
    Write-Header "Stopping All Services"

    # Kill all Java processes (Spring Boot apps)
    $javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($javaProcs) {
        Write-Step "Stopping $($javaProcs.Count) Java process(es)..."
        $javaProcs | ForEach-Object {
            Write-Info "Stopping PID $($_.Id)..."
            Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 3
        Write-Success "All Java processes stopped"
    } else {
        Write-Info "No Java processes found"
    }

    # Also kill any cmd windows running mvn
    Get-Process cmd -ErrorAction SilentlyContinue | ForEach-Object {
        try {
            $title = $_.MainWindowTitle
            if ($title -and $title -match "mvn|spring|npm|node") {
                Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
            }
        } catch {}
    }

    Write-Success "All services stopped"
}

# ---- Main Logic ----

# Handle --StopAll
if ($StopAll) {
    Stop-AllServices
    exit 0
}

# Create logs directory
if (-not (Test-Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null
}

Write-Header "Inventory Management - Microservices Startup"
Write-Info "Base directory: $BASE_DIR"
Write-Info "Logs directory: $LOG_DIR"
Write-Info "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# ---- Phase 0: Infrastructure Checks ----
if (-not $SkipInfrastructureCheck) {
    Write-Step "Phase 0: Checking infrastructure..."

    # MySQL
    if (Test-Port -Port 3306) {
        Write-Success "MySQL is running on port 3306"
    } else {
        Write-Fail "MySQL is NOT running on port 3306"
        Write-Info "Please start MySQL before continuing."
        $confirm = Read-Host "Continue anyway? (y/N)"
        if ($confirm -ne "y") { exit 1 }
    }

    # Kafka
    if (Test-Port -Port 9092) {
        Write-Success "Kafka is running on port 9092"
    } else {
        Write-Fail "Kafka is NOT running on port 9092"
        Write-Info "Please start Kafka before continuing."
        $confirm = Read-Host "Continue anyway? (y/N)"
        if ($confirm -ne "y") { exit 1 }
    }
}

if ($OnlyInfrastructure) {
    Write-Header "Startup Complete (Infrastructure Check Only)"
    exit 0
}

# ---- Phase 1: Eureka + Config Server ----
Write-Step "Phase 1: Starting infrastructure services..."
foreach ($svc in $INFRA_SERVICES) {
    if (Test-Port -Port $svc.Port) {
        Write-Info "$($svc.Name) already running on port $($svc.Port), skipping..."
    } else {
        Start-MavenService -Name $svc.Name -Dir $svc.Dir -Port $svc.Port
    }
}

# Wait for both infra services
$infraReady = $true
foreach ($svc in $INFRA_SERVICES) {
    if (-not (Wait-ForService -Name $svc.Name -Port $svc.Port -MaxWaitSec 90)) {
        $infraReady = $false
    }
}

if (-not $infraReady) {
    Write-Fail "Infrastructure services failed to start. Aborting."
    exit 1
}

# ---- Phase 2: Auth Service + API Gateway ----
Write-Step "Phase 2: Starting auth-service and api-gateway..."
foreach ($svc in $AUTH_GATEWAY_SERVICES) {
    if (Test-Port -Port $svc.Port) {
        Write-Info "$($svc.Name) already running on port $($svc.Port), skipping..."
    } else {
        Start-MavenService -Name $svc.Name -Dir $svc.Dir -Port $svc.Port
    }
}

foreach ($svc in $AUTH_GATEWAY_SERVICES) {
    if (-not (Wait-ForService -Name $svc.Name -Port $svc.Port -MaxWaitSec 90)) {
        Write-Fail "$($svc.Name) failed to start. Aborting."
        exit 1
    }
}

# ---- Phase 3: Business Services ----
Write-Step "Phase 3: Starting business services..."

# Start all business services (they can start in parallel since they all depend on eureka+config)
foreach ($svc in $BUSINESS_SERVICES) {
    if (Test-Port -Port $svc.Port) {
        Write-Info "$($svc.Name) already running on port $($svc.Port), skipping..."
    } else {
        Start-MavenService -Name $svc.Name -Dir $svc.Dir -Port $svc.Port
        Start-Sleep -Seconds 3  # Stagger starts to reduce resource contention
    }
}

# Wait for all business services
$failedServices = @()
foreach ($svc in $BUSINESS_SERVICES) {
    if (-not (Wait-ForService -Name $svc.Name -Port $svc.Port -MaxWaitSec 150)) {
        $failedServices += $svc.Name
    }
}

# ---- Phase 4: Frontend UI ----
Write-Step "Phase 4: Starting Frontend UI (inventory-dashboard)..."
$dashboardDir = Join-Path $BASE_DIR "inventory-dashboard"
if (Test-Path $dashboardDir) {
    if (Test-Port -Port 3000) {
        Write-Info "Frontend already running on port 3000, skipping..."
    } else {
        $uiLogFile = Join-Path $LOG_DIR "inventory-dashboard.log"
        Write-Info "Starting inventory-dashboard from inventory-dashboard (port 3000)..."
        Write-Info "Log: $uiLogFile"
        $uiArgs = "/c title npm-run-dev && cd /d `"$dashboardDir`" && npm install && npm run dev > `"$uiLogFile`" 2>&1"
        Start-Process cmd -ArgumentList $uiArgs -WindowStyle Minimized
        Wait-ForService -Name "inventory-dashboard" -Port 3000 -MaxWaitSec 60 -PollIntervalSec 5 | Out-Null
    }
} else {
    Write-Info "inventory-dashboard directory not found, skipping frontend."
}

# ---- Summary ----
Write-Header "Startup Summary"

$runningCount = 0
$stoppedCount = 0

foreach ($svc in $ALL_SERVICES) {
    if (Test-Port -Port $svc.Port) {
        Write-Success "$($svc.Name) - http://localhost:$($svc.Port)"
        $runningCount++
    } else {
        Write-Fail "$($svc.Name) - NOT RUNNING"
        $stoppedCount++
    }
}

Write-Host ""
Write-Host "Running: $runningCount / $($ALL_SERVICES.Count)" -ForegroundColor $(if ($stoppedCount -eq 0) { "Green" } else { "Yellow" })

if ($stoppedCount -gt 0) {
    Write-Host "Failed:  $stoppedCount" -ForegroundColor Red
    Write-Host ""
    Write-Info "Check logs in: $LOG_DIR"
}

# Eureka Dashboard
Write-Host ""
Write-Info "Eureka Dashboard: http://localhost:8761"
Write-Info "API Gateway:      http://localhost:8080"
Write-Info "Dashboard UI:     http://localhost:3000"
Write-Info "Stop all:         .\start-all-services.ps1 -StopAll"
Write-Host ""
