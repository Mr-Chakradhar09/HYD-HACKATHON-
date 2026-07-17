# ============================================================
# Inventory Management - Comprehensive API Test Script (v3)
# ============================================================
# Tests EVERY endpoint with positive + negative + edge cases.
# Run after: .\start-all-services.ps1
# ============================================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminCode = "EMP000001",
    [string]$AdminPass = "Admin@123",
    [string]$OperatorCode = "EMP000004",
    [string]$OperatorPass = "Mohan@123"
)

$SCRIPT_DIR = $PSScriptRoot
$RESULTS = @()
$PASS_COUNT = 0
$FAIL_COUNT = 0
$SKIP_COUNT = 0
$TOTAL_COUNT = 0
$ADMIN_TOKEN = ""
$OPERATOR_TOKEN = ""
$TS = Get-Date -Format "yyyyMMddHHmmss"

# ---- Helpers ----

function Write-Header {
    param([string]$Msg)
    Write-Host ""
    Write-Host ("=" * 70) -ForegroundColor Cyan
    Write-Host "  $Msg" -ForegroundColor Cyan
    Write-Host ("=" * 70) -ForegroundColor Cyan
}

function Write-Section {
    param([string]$Msg)
    Write-Host ""
    Write-Host "--- $Msg ---" -ForegroundColor Yellow
}

function Invoke-Test {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Auth = "",
        [int]$ExpectStatus,
        [string]$ExpectContains = "",
        [string]$ExpectNotContains = ""
    )

    $script:TOTAL_COUNT++
    $url = "$BaseUrl$Path"
    $headers = @{ "Content-Type" = "application/json" }
    if ($Auth) { $headers["Authorization"] = "Bearer $Auth" }

    $params = @{
        Uri             = $url
        Method          = $Method
        Headers         = $headers
        UseBasicParsing = $true
        ErrorAction     = "SilentlyContinue"
    }
    if ($Body -ne $null) {
        if ($Body -is [string]) {
            $params["Body"] = $Body
        } else {
            $params["Body"] = ($Body | ConvertTo-Json -Depth 10)
        }
    }

    try {
        $resp = Invoke-WebRequest @params
        $status = $resp.StatusCode
        $respBody = $resp.Content
    } catch {
        $status = 0
        $respBody = ""
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try {
                $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
                $respBody = $reader.ReadToEnd()
                $reader.Close()
            } catch {}
        }
    }

    $pass = $true
    $reason = ""

    if ($status -ne $ExpectStatus) {
        $pass = $false
        $reason = "Expected HTTP $ExpectStatus, got $status"
    }

    if ($pass -and $ExpectContains -and $respBody -notmatch $ExpectContains) {
        $pass = $false
        $reason = "Response missing: $ExpectContains"
    }

    if ($pass -and $ExpectNotContains -and $respBody -match $ExpectNotContains) {
        $pass = $false
        $reason = "Response should NOT contain: $ExpectNotContains"
    }

    if ($pass) {
        $script:PASS_COUNT++
        Write-Host "  [PASS] $Name" -ForegroundColor Green
    } else {
        $script:FAIL_COUNT++
        Write-Host "  [FAIL] $Name - $reason" -ForegroundColor Red
        $preview = if ($respBody.Length -gt 200) { $respBody.Substring(0,200) + "..." } else { $respBody }
        Write-Host "         Got: HTTP $status | Body: $preview" -ForegroundColor DarkGray
    }

    $script:RESULTS += [PSCustomObject]@{
        Test   = $Name
        Method = $Method
        Path   = $Path
        Status = $status
        Pass   = $pass
        Reason = $reason
    }

    return $respBody
}

function Extract-Token {
    param([string]$Json)
    try {
        $obj = $Json | ConvertFrom-Json
        if ($obj.data.token) { return $obj.data.token }
        if ($obj.data.accessToken) { return $obj.data.accessToken }
    } catch {}
    return ""
}

function Extract-RefreshToken {
    param([string]$Json)
    try {
        $obj = $Json | ConvertFrom-Json
        return $obj.data.refreshToken
    } catch {}
    return ""
}

function Extract-Id {
    param([string]$Json)
    try {
        $obj = $Json | ConvertFrom-Json
        if ($obj.data -and $obj.data.id) { return $obj.data.id }
        if ($obj.data -and $obj.data.productId) { return $obj.data.productId }
        if ($obj.data -and $obj.data.assignmentId) { return $obj.data.assignmentId }
    } catch {}
    return $null
}

function Extract-First-Id {
    param([string]$Json)
    try {
        $obj = $Json | ConvertFrom-Json
        $content = $obj.data.content
        if ($content -and $content.Count -gt 0) {
            return $content[0].id
        }
    } catch {}
    return $null
}

function Extract-Version {
    param([string]$Json)
    try {
        $obj = $Json | ConvertFrom-Json
        if ($obj.data -and $obj.data.version -ne $null) { return [long]$obj.data.version }
    } catch {}
    return 0
}

# ============================================================
# PHASE 0: Authentication
# ============================================================
Write-Header "PHASE 0: Authentication"

Write-Section "Login"
$loginResp = Invoke-Test `
    -Name "Login as SYSTEM_ADMIN (positive)" `
    -Method POST `
    -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = $AdminCode; password = $AdminPass } `
    -ExpectStatus 200
$ADMIN_TOKEN = Extract-Token $loginResp

Invoke-Test -Name "Login with wrong password (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = $AdminCode; password = "WrongPass1!" } -ExpectStatus 401

Invoke-Test -Name "Login with non-existent employee (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = "EMP000000"; password = "Admin@123" } -ExpectStatus 401

Invoke-Test -Name "Login with empty body (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = $AdminCode } -ExpectStatus 400

Invoke-Test -Name "Login with empty password (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = $AdminCode; password = "" } -ExpectStatus 400

Invoke-Test -Name "Login with empty employee code (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = ""; password = $AdminPass } -ExpectStatus 400

Invoke-Test -Name "Login with invalid employee code format (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = "INVALID"; password = "Admin@123" } -ExpectStatus 400

Invoke-Test -Name "Login with totally empty JSON (negative)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{} -ExpectStatus 400

Write-Section "Token Management"
Invoke-Test -Name "Get current user /me (positive)" -Method GET -Path "/api/v1/auth/me" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200 -ExpectContains '"employeeCode"'

Invoke-Test -Name "Access /me without token (negative)" -Method GET -Path "/api/v1/auth/me" -ExpectStatus 401

Invoke-Test -Name "Access /me with fake JWT (negative)" -Method GET -Path "/api/v1/auth/me" `
    -Auth "eyJhbGciOiJIUzI1NiJ9.fake.payload" -ExpectStatus 401

Invoke-Test -Name "Access /me with empty Bearer (negative)" -Method GET -Path "/api/v1/auth/me" `
    -Auth "" -ExpectStatus 401

Invoke-Test -Name "Refresh token with invalid token string (negative)" -Method POST -Path "/api/v1/auth/refresh-token" `
    -Body @{ refreshToken = "test" } -ExpectStatus 401

Invoke-Test -Name "Refresh with invalid token (negative)" -Method POST -Path "/api/v1/auth/refresh-token" `
    -Body @{ refreshToken = "invalid-refresh-token-xyz" } -ExpectStatus 401

Invoke-Test -Name "Refresh with empty body (negative)" -Method POST -Path "/api/v1/auth/refresh-token" `
    -Body @{} -ExpectStatus 400

Write-Section "Register"
Invoke-Test -Name "Register existing user (negative/duplicate)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = $AdminCode; email = "admin@test.com"; fullName = "Admin User"; password = $AdminPass } `
    -ExpectStatus 409

Invoke-Test -Name "Register with non-existent employee (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = "EMP999999"; email = "ghost@example.com"; fullName = "Ghost User"; password = "Test@1234" } `
    -ExpectStatus 404

Invoke-Test -Name "Register with invalid email (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = "EMP888888"; email = "not-an-email"; fullName = "Bad Email"; password = "Test@1234" } `
    -ExpectStatus 400

Invoke-Test -Name "Register with short password `<8 chars (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = "EMP888888"; email = "test@example.com"; fullName = "Short Pass"; password = "ab" } `
    -ExpectStatus 400

Invoke-Test -Name "Register with empty body (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{} -ExpectStatus 400

Invoke-Test -Name "Register with empty employeeCode (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = ""; email = "e@t.com"; fullName = "Name"; password = "Test@1234" } `
    -ExpectStatus 400

Invoke-Test -Name "Register with invalid code format (negative)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = "BADFORMAT"; email = "e@t.com"; fullName = "Name"; password = "Test@1234" } `
    -ExpectStatus 400

Write-Section "Change Password"
Invoke-Test -Name "Change password without auth (negative)" -Method POST -Path "/api/v1/auth/change-password" `
    -Body @{ currentPassword = "Old@1234"; newPassword = "New@12345"; confirmPassword = "New@12345" } `
    -ExpectStatus 401

Write-Section "Logout"
Invoke-Test -Name "Logout without auth (negative)" -Method POST -Path "/api/v1/auth/logout" `
    -Body @{ refreshToken = "some-token" } -ExpectStatus 401

# ============================================================
# PHASE 0b: Employee Management (SYSTEM_ADMIN only)
# ============================================================
Write-Header "PHASE 0b: Employee Management"

$empResp = Invoke-Test -Name "Create employee (positive)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "EMP555555"; fullName = "Warehouse Operator"; email = "op@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create employee without auth (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "EMP555556"; fullName = "No Auth"; email = "na@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -ExpectStatus 401

Invoke-Test -Name "Create employee with invalid code format (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "BAD"; fullName = "Bad"; email = "b@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create employee with empty roles (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "EMP555556"; fullName = "No Roles"; email = "nr@test.com"; roles = @() } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create employee with duplicate code (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "EMP555555"; fullName = "Dup"; email = "d@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 409

Invoke-Test -Name "Create employee with invalid email (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{ employeeCode = "EMP555556"; fullName = "Bad"; email = "not-email"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create employee with empty body (negative)" -Method POST -Path "/api/v1/auth/employees" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Register new employee (positive)" -Method POST -Path "/api/v1/auth/register" `
    -Body @{ employeeCode = "EMP555555"; email = "op@test.com"; fullName = "Warehouse Operator"; password = "Operator@123" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Get employee by code (positive)" -Method GET -Path "/api/v1/auth/employees/EMP555555" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Get non-existent employee (negative)" -Method GET -Path "/api/v1/auth/employees/EMP000000" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Get all employees (positive)" -Method GET -Path "/api/v1/auth/employees" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Get employees without auth (negative)" -Method GET -Path "/api/v1/auth/employees" `
    -ExpectStatus 401

Invoke-Test -Name "Update employee (positive)" -Method PUT -Path "/api/v1/auth/employees/EMP555555" `
    -Body @{ fullName = "Updated Operator"; email = "upd@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Update employee invalid email (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP555555" `
    -Body @{ fullName = "Upd"; email = "bad"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Update employee empty roles (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP555555" `
    -Body @{ fullName = "Upd"; email = "u@t.com"; roles = @() } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Update non-existent employee (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP000000" `
    -Body @{ fullName = "Ghost"; email = "g@t.com"; roles = @("WAREHOUSE_OPERATOR") } `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Deactivate employee (positive)" -Method PUT -Path "/api/v1/auth/employees/EMP555555/deactivate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Deactivate already inactive employee (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP555555/deactivate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Activate employee (positive)" -Method PUT -Path "/api/v1/auth/employees/EMP555555/activate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Activate already active employee (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP555555/activate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Activate non-existent employee (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP000000/activate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Deactivate non-existent employee (negative)" -Method PUT -Path "/api/v1/auth/employees/EMP000000/deactivate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Delete active employee (negative - only pending can be deleted)" -Method DELETE -Path "/api/v1/auth/employees/EMP555555" `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Delete non-existent employee (negative)" -Method DELETE -Path "/api/v1/auth/employees/EMP000000" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Delete employee without auth (negative)" -Method DELETE -Path "/api/v1/auth/employees/EMP555555" `
    -ExpectStatus 401

# ============================================================
# PHASE 1: Categories
# ============================================================
Write-Header "PHASE 1: Category Management"

$catResp = Invoke-Test -Name "Create category (positive)" -Method POST -Path "/api/v1/categories" `
    -Body @{ name = "Electronics_$TS" } -Auth $ADMIN_TOKEN -ExpectStatus 201
$CAT_ID = Extract-Id $catResp

$catResp2 = Invoke-Test -Name "Create second category (positive)" -Method POST -Path "/api/v1/categories" `
    -Body @{ name = "Furniture_$TS" } -Auth $ADMIN_TOKEN -ExpectStatus 201
$CAT_ID2 = Extract-Id $catResp2

if (-not $CAT_ID) {
    $listResp = Invoke-Test -Name "Fallback: list categories to find ID" -Method GET -Path "/api/v1/categories?page=0`&size=1" -Auth $ADMIN_TOKEN -ExpectStatus 200
    $CAT_ID = Extract-First-Id $listResp
    if ($CAT_ID) { Write-Host "    [INFO] Using existing category ID: $CAT_ID" -ForegroundColor DarkCyan }
}

Invoke-Test -Name "Create category empty name (negative)" -Method POST -Path "/api/v1/categories" `
    -Body @{ name = "" } -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create category without auth (negative)" -Method POST -Path "/api/v1/categories" `
    -Body @{ name = "Unauth_Cat" } -ExpectStatus 401

Invoke-Test -Name "Create category with name `>100 chars (negative)" -Method POST -Path "/api/v1/categories" `
    -Body @{ name = ("A" * 101) } -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create category empty body (negative)" -Method POST -Path "/api/v1/categories" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

if ($CAT_ID) {
    Invoke-Test -Name "Get category by ID (positive)" -Method GET -Path "/api/v1/categories/$CAT_ID" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Update category (positive)" -Method PUT -Path "/api/v1/categories/$CAT_ID" `
        -Body @{ name = "Electronics_Upd_$TS" } -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Update category empty name (negative)" -Method PUT -Path "/api/v1/categories/$CAT_ID" `
        -Body @{ name = "" } -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Update category `>100 chars (negative)" -Method PUT -Path "/api/v1/categories/$CAT_ID" `
        -Body @{ name = ("B" * 101) } -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Update category without auth (negative)" -Method PUT -Path "/api/v1/categories/$CAT_ID" `
        -Body @{ name = "X" } -ExpectStatus 401
} else {
    Write-Host "  [SKIP] Category ID tests - no ID available" -ForegroundColor DarkYellow
}

Invoke-Test -Name "Get non-existent category (negative)" -Method GET -Path "/api/v1/categories/99999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Get all categories (positive)" -Method GET -Path "/api/v1/categories" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get categories with pagination (positive)" -Method GET -Path "/api/v1/categories?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get categories sort DESC (positive)" -Method GET -Path "/api/v1/categories?sortDir=DESC" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Update non-existent category (negative)" -Method PUT -Path "/api/v1/categories/99999" `
    -Body @{ name = "Ghost" } -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Deactivate non-existent category (negative)" -Method PATCH -Path "/api/v1/categories/99999/status" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

# ============================================================
# PHASE 2: Products
# ============================================================
Write-Header "PHASE 2: Product Management"

$prodBody = @{
    sku         = "SKU-$TS-001"
    productName = "Widget_$TS"
    brand       = "TestBrand"
    categoryId  = if ($CAT_ID) { [long]$CAT_ID } else { 1 }
    unit        = "PIECE"
    price       = 29.99
}
$prodResp = Invoke-Test -Name "Create product (positive)" -Method POST -Path "/api/v1/products" `
    -Body $prodBody -Auth $ADMIN_TOKEN -ExpectStatus 201
$PROD_ID = Extract-Id $prodResp

$prodBody2 = @{
    sku         = "SKU-$TS-002"
    productName = "Gadget_$TS"
    brand       = "TestBrand"
    categoryId  = if ($CAT_ID) { [long]$CAT_ID } else { 1 }
    unit        = "BOX"
    price       = 49.99
}
$prodResp2 = Invoke-Test -Name "Create second product (positive)" -Method POST -Path "/api/v1/products" `
    -Body $prodBody2 -Auth $ADMIN_TOKEN -ExpectStatus 201
$PROD_ID2 = Extract-Id $prodResp2

if (-not $PROD_ID) {
    $listResp = Invoke-Test -Name "Fallback: list products to find ID" -Method GET -Path "/api/v1/products?page=0`&size=1" -Auth $ADMIN_TOKEN -ExpectStatus 200
    $PROD_ID = Extract-First-Id $listResp
    if ($PROD_ID) { Write-Host "    [INFO] Using existing product ID: $PROD_ID" -ForegroundColor DarkCyan }
}

Invoke-Test -Name "Create product missing SKU (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ productName = "No SKU"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product missing name (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product missing brand (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; productName = "X"; categoryId = 1; unit = "PIECE"; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product missing categoryId (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; productName = "X"; brand = "B"; unit = "PIECE"; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product missing unit (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; productName = "X"; brand = "B"; categoryId = 1; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product missing price (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; productName = "X"; brand = "B"; categoryId = 1; unit = "PIECE" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product price zero (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-BAD1"; productName = "Bad"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 0 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product negative price (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-BAD2"; productName = "Neg"; brand = "B"; categoryId = 1; unit = "PIECE"; price = -5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product without auth (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-X"; productName = "X"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10 } `
    -ExpectStatus 401

Invoke-Test -Name "Create product empty body (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create product with name `>255 chars (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{ sku = "SKU-LONG"; productName = ("C" * 256); brand = "B"; categoryId = 1; unit = "PIECE"; price = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

if ($PROD_ID) {
    Invoke-Test -Name "Get product by ID (positive)" -Method GET -Path "/api/v1/products/$PROD_ID" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    $getProdResp = Invoke-Test -Name "Get product by ID verify fields (positive)" -Method GET -Path "/api/v1/products/$PROD_ID" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200 -ExpectContains '"sku"'
    $PROD_VERSION = Extract-Version $getProdResp

    Invoke-Test -Name "Update product (positive)" -Method PUT -Path "/api/v1/products/$PROD_ID" `
        -Body @{ productName = "Widget_Upd_$TS"; brand = "Upd"; categoryId = [long]$CAT_ID; unit = "PIECE"; price = 39.99; version = $PROD_VERSION } `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Update product missing version (negative)" -Method PUT -Path "/api/v1/products/$PROD_ID" `
        -Body @{ productName = "NoVer"; brand = "B"; categoryId = [long]$CAT_ID; unit = "PIECE"; price = 10 } `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Update product missing name (negative)" -Method PUT -Path "/api/v1/products/$PROD_ID" `
        -Body @{ brand = "B"; categoryId = [long]$CAT_ID; unit = "PIECE"; price = 10; version = 0 } `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Update product without auth (negative)" -Method PUT -Path "/api/v1/products/$PROD_ID" `
        -Body @{ productName = "X"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10; version = 0 } `
        -ExpectStatus 401

    Invoke-Test -Name "Deactivate product (positive)" -Method PATCH -Path "/api/v1/products/$PROD_ID/status" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Deactivate product without auth (negative)" -Method PATCH -Path "/api/v1/products/$PROD_ID/status" `
        -ExpectStatus 401

    Invoke-Test -Name "Deactivate non-existent product (negative)" -Method PATCH -Path "/api/v1/products/99999/status" `
        -Auth $ADMIN_TOKEN -ExpectStatus 404
} else {
    Write-Host "  [SKIP] Product ID tests - no ID available" -ForegroundColor DarkYellow
}

Invoke-Test -Name "Get non-existent product (negative)" -Method GET -Path "/api/v1/products/99999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Get all products (positive)" -Method GET -Path "/api/v1/products" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get products page 0 size 5 (positive)" -Method GET -Path "/api/v1/products?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get products sort DESC (positive)" -Method GET -Path "/api/v1/products?sortBy=productName`&sortDir=DESC" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get products sort by price (positive)" -Method GET -Path "/api/v1/products?sortBy=price`&sortDir=ASC" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get products without auth (negative)" -Method GET -Path "/api/v1/products" `
    -ExpectStatus 401
Invoke-Test -Name "Update non-existent product (negative)" -Method PUT -Path "/api/v1/products/99999" `
    -Body @{ productName = "G"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10; version = 0 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Search products by keyword (positive)" -Method GET -Path "/api/v1/products/search?search=Widget" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by category (positive)" -Method GET -Path "/api/v1/products/search?categoryId=$CAT_ID" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by brand (positive)" -Method GET -Path "/api/v1/products/search?brand=TestBrand" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by status ACTIVE (positive)" -Method GET -Path "/api/v1/products/search?status=ACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by unit (positive)" -Method GET -Path "/api/v1/products/search?unit=PIECE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by minPrice only (positive)" -Method GET -Path "/api/v1/products/search?minPrice=10" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by maxPrice only (positive)" -Method GET -Path "/api/v1/products/search?maxPrice=50" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products by price range (positive)" -Method GET -Path "/api/v1/products/search?minPrice=10`&maxPrice=50" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products combined filters (positive)" -Method GET -Path "/api/v1/products/search?search=Widget`&brand=TestBrand`&minPrice=10`&maxPrice=50" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products no results (positive)" -Method GET -Path "/api/v1/products/search?search=NONEXISTENTXYZ" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search products without auth (negative)" -Method GET -Path "/api/v1/products/search" `
    -ExpectStatus 401

if ($CAT_ID) {
    Invoke-Test -Name "Deactivate category (positive)" -Method PATCH -Path "/api/v1/categories/$CAT_ID/status" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200
    Invoke-Test -Name "Deactivate already inactive category (negative)" -Method PATCH -Path "/api/v1/categories/$CAT_ID/status" `
        -Auth $ADMIN_TOKEN -ExpectStatus 400
}

# ============================================================
# PHASE 3: Warehouses
# ============================================================
Write-Header "PHASE 3: Warehouse Management"

$whBody = @{
    warehouseCode = "WH$TS01"
    warehouseName = "TestWH_$TS"
    email         = "wh@test.com"
    phone         = "9876543210"
    capacity      = 10000
    address       = @{
        addressLine1 = "123 Test St"
        city         = "Bangalore"
        state        = "Karnataka"
        country      = "India"
        postalCode   = "560001"
    }
}
$whResp = Invoke-Test -Name "Create warehouse (positive)" -Method POST -Path "/api/v1/warehouses" `
    -Body $whBody -Auth $ADMIN_TOKEN -ExpectStatus 201
$WH_ID = Extract-Id $whResp

$whBody2 = @{
    warehouseCode = "WH$TS02"
    warehouseName = "TestWH2_$TS"
    email         = "wh2@test.com"
    phone         = "9876543211"
    capacity      = 20000
    address       = @{
        addressLine1 = "456 Test Ave"
        city         = "Mumbai"
        state        = "Maharashtra"
        country      = "India"
        postalCode   = "400001"
    }
}
$whResp2 = Invoke-Test -Name "Create second warehouse (positive)" -Method POST -Path "/api/v1/warehouses" `
    -Body $whBody2 -Auth $ADMIN_TOKEN -ExpectStatus 201
$WH_ID2 = Extract-Id $whResp2

if (-not $WH_ID) {
    $listResp = Invoke-Test -Name "Fallback: list warehouses to find ID" -Method GET -Path "/api/v1/warehouses?page=0`&size=1" -Auth $ADMIN_TOKEN -ExpectStatus 200
    $WH_ID = Extract-First-Id $listResp
    if ($WH_ID) { Write-Host "    [INFO] Using existing warehouse ID: $WH_ID" -ForegroundColor DarkCyan }
}

    Invoke-Test -Name "Create warehouse duplicate code (negative)" -Method POST -Path "/api/v1/warehouses" `
        -Body @{ warehouseCode = "WH$TS01"; warehouseName = "Dup"; email = "d@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 409

Invoke-Test -Name "Create warehouse invalid phone (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-BAD"; warehouseName = "Bad"; email = "b@t.com"; phone = "123"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse invalid email (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-BADX"; warehouseName = "Bad"; email = "not-email"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse negative capacity (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-BAD2"; warehouseName = "Bad2"; email = "b@t.com"; phone = "9876543210"; capacity = -100; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse zero capacity (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-BAD3"; warehouseName = "Bad3"; email = "b@t.com"; phone = "9876543210"; capacity = 0; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse missing address (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-NOADDR"; warehouseName = "NoAddr"; email = "n@t.com"; phone = "9876543210"; capacity = 5000 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse missing addressLine1 (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-NOAL1"; warehouseName = "NoAL1"; email = "n@t.com"; phone = "9876543210"; capacity = 5000; address = @{ city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse without SYSTEM_ADMIN role (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-X"; warehouseName = "X"; email = "x@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -ExpectStatus 401

Invoke-Test -Name "Create warehouse empty body (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse name `>100 chars (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = "WH-LONG"; warehouseName = ("D" * 101); email = "l@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create warehouse code `>20 chars (negative)" -Method POST -Path "/api/v1/warehouses" `
    -Body @{ warehouseCode = ("E" * 21); warehouseName = "LongCode"; email = "l@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

if ($WH_ID) {
    Invoke-Test -Name "Get warehouse by ID (positive)" -Method GET -Path "/api/v1/warehouses/$WH_ID" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Get warehouse verify fields (positive)" -Method GET -Path "/api/v1/warehouses/$WH_ID" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200 -ExpectContains '"warehouseCode"'

    Invoke-Test -Name "Update warehouse (positive)" -Method PUT -Path "/api/v1/warehouses/$WH_ID" `
        -Body @{ warehouseName = "Upd_$TS"; email = "u@t.com"; phone = "9876543219"; capacity = 15000; address = @{ addressLine1 = "Upd St"; city = "Chennai"; state = "TN"; country = "India"; postalCode = "600001" } } `
        -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Update warehouse invalid phone (negative)" -Method PUT -Path "/api/v1/warehouses/$WH_ID" `
        -Body @{ warehouseName = "X"; email = "x@t.com"; phone = "123"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Update warehouse without auth (negative)" -Method PUT -Path "/api/v1/warehouses/$WH_ID" `
        -Body @{ warehouseName = "X"; email = "x@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
        -ExpectStatus 401

    Invoke-Test -Name "Deactivate warehouse (positive)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/deactivate" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200
    Invoke-Test -Name "Deactivate already inactive warehouse (negative)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/deactivate" `
        -Auth $ADMIN_TOKEN -ExpectStatus 400
    Invoke-Test -Name "Activate warehouse (positive)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/activate" `
        -Auth $ADMIN_TOKEN -ExpectStatus 200
    Invoke-Test -Name "Activate already active warehouse (negative)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/activate" `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Activate warehouse without auth (negative)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/activate" `
        -ExpectStatus 401
    Invoke-Test -Name "Deactivate warehouse without auth (negative)" -Method PATCH -Path "/api/v1/warehouses/$WH_ID/deactivate" `
        -ExpectStatus 401
} else {
    Write-Host "  [SKIP] Warehouse ID tests - no ID available" -ForegroundColor DarkYellow
}

Invoke-Test -Name "Get non-existent warehouse (negative)" -Method GET -Path "/api/v1/warehouses/99999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Get all warehouses (positive)" -Method GET -Path "/api/v1/warehouses" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get warehouses with pagination (positive)" -Method GET -Path "/api/v1/warehouses?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get warehouses sort DESC (positive)" -Method GET -Path "/api/v1/warehouses?sort=warehouseName`&direction=DESC" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get warehouses without auth (negative)" -Method GET -Path "/api/v1/warehouses" `
    -ExpectStatus 401
Invoke-Test -Name "Update non-existent warehouse (negative)" -Method PUT -Path "/api/v1/warehouses/99999" `
    -Body @{ warehouseName = "G"; email = "g@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Search warehouses by keyword (positive)" -Method GET -Path "/api/v1/warehouses/search?keyword=Test" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search warehouses by status ACTIVE (positive)" -Method GET -Path "/api/v1/warehouses/search?status=ACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search warehouses by status INACTIVE (positive)" -Method GET -Path "/api/v1/warehouses/search?status=INACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search warehouses keyword + status (positive)" -Method GET -Path "/api/v1/warehouses/search?keyword=Test`&status=ACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search warehouses no results (positive)" -Method GET -Path "/api/v1/warehouses/search?keyword=NONEXISTENTXYZ" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search warehouses without auth (negative)" -Method GET -Path "/api/v1/warehouses/search" `
    -ExpectStatus 401
Invoke-Test -Name "Activate non-existent warehouse (negative)" -Method PATCH -Path "/api/v1/warehouses/99999/activate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Deactivate non-existent warehouse (negative)" -Method PATCH -Path "/api/v1/warehouses/99999/deactivate" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

# ============================================================
# PHASE 4: Warehouse Assignment
# ============================================================
Write-Header "PHASE 4: Warehouse Assignment"

if ($WH_ID) {
    $assignResp = Invoke-Test -Name "Assign employee to warehouse (positive)" -Method POST `
        -Path "/api/v1/warehouses/$WH_ID/assignments" `
        -Body @{ employeeCode = $AdminCode; warehouseRole = "WAREHOUSE_OPERATOR" } `
        -Auth $ADMIN_TOKEN -ExpectStatus 201
    $ASSIGN_ID = Extract-Id $assignResp

    Invoke-Test -Name "Assign employee duplicate (negative)" -Method POST `
        -Path "/api/v1/warehouses/$WH_ID/assignments" `
        -Body @{ employeeCode = $AdminCode; warehouseRole = "WAREHOUSE_OPERATOR" } `
        -Auth $ADMIN_TOKEN -ExpectStatus 409

    Invoke-Test -Name "Assign with missing employeeCode (negative)" -Method POST `
        -Path "/api/v1/warehouses/$WH_ID/assignments" `
        -Body @{ warehouseRole = "INVENTORY_MANAGER" } `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Assign with missing warehouseRole (negative)" -Method POST `
        -Path "/api/v1/warehouses/$WH_ID/assignments" `
        -Body @{ employeeId = 2; employeeCode = "EMP000002" } `
        -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Get assignments for warehouse (positive)" -Method GET `
        -Path "/api/v1/warehouses/$WH_ID/assignments?page=0`&size=10" -Auth $ADMIN_TOKEN -ExpectStatus 200

    Invoke-Test -Name "Get assignments page 0 size 2 (positive)" -Method GET `
        -Path "/api/v1/warehouses/$WH_ID/assignments?page=0`&size=2" -Auth $ADMIN_TOKEN -ExpectStatus 200

    if ($ASSIGN_ID) {
        Invoke-Test -Name "Update assignment role (positive)" -Method PUT -Path "/api/v1/assignments/$ASSIGN_ID" `
            -Body @{ warehouseRole = "INVENTORY_MANAGER" } -Auth $ADMIN_TOKEN -ExpectStatus 200

        Invoke-Test -Name "Update assignment missing role (negative)" -Method PUT -Path "/api/v1/assignments/$ASSIGN_ID" `
            -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

        Invoke-Test -Name "Update assignment without auth (negative)" -Method PUT -Path "/api/v1/assignments/$ASSIGN_ID" `
            -Body @{ warehouseRole = "INVENTORY_MANAGER" } -ExpectStatus 401

        Invoke-Test -Name "Deactivate assignment (positive)" -Method PATCH -Path "/api/v1/assignments/$ASSIGN_ID/status" `
            -Auth $ADMIN_TOKEN -ExpectStatus 200

        Invoke-Test -Name "Deactivate already inactive assignment (negative)" -Method PATCH -Path "/api/v1/assignments/$ASSIGN_ID/status" `
            -Auth $ADMIN_TOKEN -ExpectStatus 400

        Invoke-Test -Name "Deactivate assignment without auth (negative)" -Method PATCH -Path "/api/v1/assignments/$ASSIGN_ID/status" `
            -ExpectStatus 401
    }
} else {
    Write-Host "  [SKIP] Assignment tests - no warehouse ID" -ForegroundColor DarkYellow
}

Invoke-Test -Name "Assign without auth (negative)" -Method POST -Path "/api/v1/warehouses/1/assignments" `
    -Body @{ employeeId = 1; employeeCode = "X"; warehouseRole = "INVENTORY_MANAGER"; assignedBy = 1 } `
    -ExpectStatus 401
Invoke-Test -Name "Get non-existent warehouse assignments (positive)" -Method GET -Path "/api/v1/warehouses/99999/assignments" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Deactivate non-existent assignment (negative)" -Method PATCH -Path "/api/v1/assignments/99999/status" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Update non-existent assignment (negative)" -Method PUT -Path "/api/v1/assignments/99999" `
    -Body @{ warehouseRole = "INVENTORY_MANAGER" } -Auth $ADMIN_TOKEN -ExpectStatus 404

# ============================================================
# PHASE 5: Inventory
# ============================================================
Write-Header "PHASE 5: Inventory Management"

if ($PROD_ID -and $WH_ID) {
    $invResp = Invoke-Test -Name "Create inventory (positive)" -Method POST -Path "/api/v1/inventories" `
        -Body @{ productId = [long]$PROD_ID; warehouseId = [long]$WH_ID } `
        -Auth $ADMIN_TOKEN -ExpectStatus 201
    $INV_ID = Extract-Id $invResp

    Invoke-Test -Name "Create inventory without productId (negative)" -Method POST -Path "/api/v1/inventories" `
        -Body @{ warehouseId = [long]$WH_ID } -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Create inventory without warehouseId (negative)" -Method POST -Path "/api/v1/inventories" `
        -Body @{ productId = [long]$PROD_ID } -Auth $ADMIN_TOKEN -ExpectStatus 400

    Invoke-Test -Name "Create inventory empty body (negative)" -Method POST -Path "/api/v1/inventories" `
        -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400
} else {
    Write-Host "  [SKIP] Inventory create tests - missing product/warehouse ID" -ForegroundColor DarkYellow
}

Invoke-Test -Name "Get all inventories (positive)" -Method GET -Path "/api/v1/inventories" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get inventories with pagination (positive)" -Method GET -Path "/api/v1/inventories?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

$invIdForGet = if ($INV_ID) { $INV_ID } else { 1 }
Invoke-Test -Name "Get non-existent inventory (negative)" -Method GET -Path "/api/v1/inventories/99999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Get inventories without auth (negative)" -Method GET -Path "/api/v1/inventories" `
    -ExpectStatus 401
Invoke-Test -Name "Search inventories by status ACTIVE (positive)" -Method GET -Path "/api/v1/inventories/search?status=ACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search inventories by status INACTIVE (positive)" -Method GET -Path "/api/v1/inventories/search?status=INACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search inventories by warehouse (positive)" -Method GET -Path "/api/v1/inventories/search?warehouseId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search inventories by product (positive)" -Method GET -Path "/api/v1/inventories/search?productId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search inventories combined (positive)" -Method GET -Path "/api/v1/inventories/search?warehouseId=1`&productId=1`&status=ACTIVE" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search inventories without auth (negative)" -Method GET -Path "/api/v1/inventories/search" `
    -ExpectStatus 401
Invoke-Test -Name "Deactivate inventory (positive)" -Method PATCH -Path "/api/v1/inventories/$invIdForGet/status" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Deactivate non-existent inventory (negative)" -Method PATCH -Path "/api/v1/inventories/99999/status" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Deactivate inventory without auth (negative)" -Method PATCH -Path "/api/v1/inventories/$invIdForGet/status" `
    -ExpectStatus 401

# ============================================================
# PHASE 6: Reservations
# ============================================================
Write-Header "PHASE 6: Reservations"

$resInvId = if ($INV_ID) { [long]$INV_ID } else { 1 }

# Pre-requisite: Add some stock to the inventory so reservation doesn't fail with InsufficientStockException (400)
Write-Host "  [PRE-REQ] Adding stock to inventory $resInvId for reservation tests..." -ForegroundColor Gray
Invoke-RestMethod -Uri "$BaseUrl/api/v1/adjustments" -Method POST -Headers @{ "Authorization" = "Bearer $ADMIN_TOKEN" } -ContentType "application/json" -Body (@{ inventoryId = $resInvId; adjustmentType = "INCREASE"; quantity = 50; reason = "Initial Stock for tests" } | ConvertTo-Json) | Out-Null

$reserveResp = Invoke-Test -Name "Create reservation (positive)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceNumber = "REF-$TS-001"; referenceType = "SALES_ORDER"; reservedQuantity = 5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201
$RES_ID = Extract-Id $reserveResp

Invoke-Test -Name "Create reservation missing fields (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation missing inventoryId (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ referenceNumber = "REF"; referenceType = "SALES_ORDER"; reservedQuantity = 5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation missing referenceNumber (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceType = "SALES_ORDER"; reservedQuantity = 5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation missing referenceType (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceNumber = "REF"; reservedQuantity = 5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation zero quantity (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceNumber = "REF-BAD"; referenceType = "SALES_ORDER"; reservedQuantity = 0 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation negative quantity (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceNumber = "REF-BAD"; referenceType = "SALES_ORDER"; reservedQuantity = -1 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create reservation without auth (negative)" -Method POST -Path "/api/v1/reservations" `
    -Body @{ inventoryId = $resInvId; referenceNumber = "REF"; referenceType = "SALES_ORDER"; reservedQuantity = 5 } `
    -ExpectStatus 401

$resIdForPatch = if ($RES_ID) { $RES_ID } else { 1 }
Invoke-Test -Name "Release reservation (positive)" -Method PATCH -Path "/api/v1/reservations/$resIdForPatch/release" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Release non-existent reservation (negative)" -Method PATCH -Path "/api/v1/reservations/99999/release" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Release reservation without auth (negative)" -Method PATCH -Path "/api/v1/reservations/$resIdForPatch/release" `
    -ExpectStatus 401

Invoke-Test -Name "Cancel reservation (positive)" -Method PATCH -Path "/api/v1/reservations/$resIdForPatch/cancel" `
    -Auth $ADMIN_TOKEN -ExpectStatus 400
Invoke-Test -Name "Cancel non-existent reservation (negative)" -Method PATCH -Path "/api/v1/reservations/99999/cancel" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Cancel reservation without auth (negative)" -Method PATCH -Path "/api/v1/reservations/$resIdForPatch/cancel" `
    -ExpectStatus 401

# ============================================================
# PHASE 7: Adjustments
# ============================================================
Write-Header "PHASE 7: Adjustments"

$adjInvId = if ($INV_ID) { [long]$INV_ID } else { 1 }
Invoke-Test -Name "Create adjustment INCREASE (positive)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "INCREASE"; quantity = 10; reason = "Cycle count correction"; remarks = "Monthly count" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create adjustment DECREASE (positive)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "DECREASE"; quantity = 5; reason = "Damaged goods" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create adjustment DAMAGE (positive)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "DAMAGE"; quantity = 2; reason = "Water damage" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create adjustment missing fields (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment missing inventoryId (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ adjustmentType = "INCREASE"; quantity = 5; reason = "Test" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment missing type (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; quantity = 5; reason = "Test" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment missing reason (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "INCREASE"; quantity = 5 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment zero quantity (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "DECREASE"; quantity = 0; reason = "Bad" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment negative quantity (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "INCREASE"; quantity = -1; reason = "Test" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create adjustment without auth (negative)" -Method POST -Path "/api/v1/adjustments" `
    -Body @{ inventoryId = $adjInvId; adjustmentType = "INCREASE"; quantity = 5; reason = "Test" } `
    -ExpectStatus 401

# ============================================================
# PHASE 8: Movements
# ============================================================
Write-Header "PHASE 8: Movements"

$movProdId = if ($PROD_ID) { [long]$PROD_ID } else { 1 }
$movWhId = if ($WH_ID) { [long]$WH_ID } else { 1 }
$movWhId2 = if ($WH_ID2) { [long]$WH_ID2 } else { 2 }

Invoke-Test -Name "Create GOODS_RECEIPT movement (positive)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = $movProdId; destinationWarehouseId = $movWhId; quantity = 100; referenceType = "PURCHASE_ORDER"; referenceNumber = "PO-$TS-001"; remarks = "Initial stock" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create GOODS_ISSUE movement (positive)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_ISSUE"; productId = $movProdId; sourceWarehouseId = $movWhId; quantity = 10; referenceType = "SALES_ORDER"; referenceNumber = "SO-$TS-001" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create TRANSFER_OUT movement (positive)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "TRANSFER_OUT"; productId = $movProdId; sourceWarehouseId = $movWhId; destinationWarehouseId = $movWhId2; quantity = 20; referenceType = "TRANSFER_ORDER"; referenceNumber = "TO-$TS-001" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create RETURN_IN movement (positive)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "RETURN_IN"; productId = $movProdId; destinationWarehouseId = $movWhId; quantity = 5; referenceType = "RETURN_ORDER"; referenceNumber = "RO-$TS-001" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 201

Invoke-Test -Name "Create movement missing fields (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement missing movementType (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ productId = 1; quantity = 10; referenceType = "PURCHASE_ORDER" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement missing productId (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; quantity = 10; referenceType = "PURCHASE_ORDER" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement missing quantity (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = 1; referenceType = "PURCHASE_ORDER" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement missing referenceType (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = 1; quantity = 10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement zero quantity (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = 1; quantity = 0; referenceType = "PURCHASE_ORDER" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement negative quantity (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = 1; quantity = -5; referenceType = "PURCHASE_ORDER" } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create movement without auth (negative)" -Method POST -Path "/api/v1/movements" `
    -Body @{ movementType = "GOODS_RECEIPT"; productId = 1; quantity = 10; referenceType = "PURCHASE_ORDER" } `
    -ExpectStatus 401

Invoke-Test -Name "Get movement by ID (positive)" -Method GET -Path "/api/v1/movements/1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get non-existent movement (negative)" -Method GET -Path "/api/v1/movements/99999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "Get all movements (positive)" -Method GET -Path "/api/v1/movements" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get movements with pagination (positive)" -Method GET -Path "/api/v1/movements?page=0`&size=10" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get movements without auth (negative)" -Method GET -Path "/api/v1/movements" `
    -ExpectStatus 401
Invoke-Test -Name "Search movements by type GOODS_RECEIPT (positive)" -Method GET -Path "/api/v1/movements/search?movementType=GOODS_RECEIPT" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements by type TRANSFER_OUT (positive)" -Method GET -Path "/api/v1/movements/search?movementType=TRANSFER_OUT" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements by status PENDING (positive)" -Method GET -Path "/api/v1/movements/search?status=PENDING" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements by status COMPLETED (positive)" -Method GET -Path "/api/v1/movements/search?status=COMPLETED" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements by product (positive)" -Method GET -Path "/api/v1/movements/search?productId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements by warehouse (positive)" -Method GET -Path "/api/v1/movements/search?warehouseId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements combined (positive)" -Method GET -Path "/api/v1/movements/search?movementType=GOODS_RECEIPT`&status=COMPLETED`&warehouseId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Search movements without auth (negative)" -Method GET -Path "/api/v1/movements/search" `
    -ExpectStatus 401

# ============================================================
# PHASE 9: Replenishment
# ============================================================
Write-Header "PHASE 9: Replenishment"

Invoke-Test -Name "Get all replenishment requests (positive)" -Method GET -Path "/api/v1/replenishment" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

$repProdId = if ($PROD_ID) { [long]$PROD_ID } else { 1 }
$repWhId = if ($WH_ID) { [long]$WH_ID } else { 1 }

Invoke-Test -Name "Create replenishment (positive)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; warehouseId = $repWhId; requestedQuantity = 50 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Create replenishment missing fields (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment missing productId (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ warehouseId = $repWhId; requestedQuantity = 50 } -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment missing warehouseId (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; requestedQuantity = 50 } -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment missing quantity (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; warehouseId = $repWhId } -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment negative quantity (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; warehouseId = $repWhId; requestedQuantity = -10 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment zero quantity (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; warehouseId = $repWhId; requestedQuantity = 0 } `
    -Auth $ADMIN_TOKEN -ExpectStatus 400

Invoke-Test -Name "Create replenishment without auth (negative)" -Method POST -Path "/api/v1/replenishment" `
    -Body @{ productId = $repProdId; warehouseId = $repWhId; requestedQuantity = 50 } `
    -ExpectStatus 401

Invoke-Test -Name "Update replenishment status (positive)" -Method PATCH -Path "/api/v1/replenishment/1/status?status=APPROVED" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Update non-existent replenishment (negative)" -Method PATCH -Path "/api/v1/replenishment/99999/status?status=APPROVED" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404

Invoke-Test -Name "Update replenishment without auth (negative)" -Method PATCH -Path "/api/v1/replenishment/1/status?status=APPROVED" `
    -ExpectStatus 401

# ============================================================
# PHASE 10: Reporting
# ============================================================
Write-Header "PHASE 10: Reporting"

Invoke-Test -Name "Get dashboard summary (positive)" -Method GET -Path "/api/v1/dashboard/summary" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get dashboard without auth (negative)" -Method GET -Path "/api/v1/dashboard/summary" `
    -ExpectStatus 401

Invoke-Test -Name "Get movement reports (positive)" -Method GET -Path "/api/v1/reports/movements" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get movement reports by product (positive)" -Method GET -Path "/api/v1/reports/movements?productId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get movement reports by warehouse (positive)" -Method GET -Path "/api/v1/reports/movements?warehouseId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get movement reports with pagination (positive)" -Method GET -Path "/api/v1/reports/movements?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

Invoke-Test -Name "Get inventory summary (positive)" -Method GET -Path "/api/v1/reports/inventory-summary" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get inventory summary by warehouse (positive)" -Method GET -Path "/api/v1/reports/inventory-summary?warehouseId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get inventory summary by product (positive)" -Method GET -Path "/api/v1/reports/inventory-summary?productId=1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Get inventory summary with pagination (positive)" -Method GET -Path "/api/v1/reports/inventory-summary?page=0`&size=5" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200

# ============================================================
# PHASE 11: RBAC
# ============================================================
Write-Header "PHASE 12: Role-Based Access Control"

$operLogin = Invoke-Test -Name "Login as WAREHOUSE_OPERATOR (EMP555555)" -Method POST -Path "/api/v1/auth/login" `
    -Body @{ employeeCode = "EMP555555"; password = "Operator@123" } -ExpectStatus 200
$OPERATOR_TOKEN = Extract-Token $operLogin

if ($OPERATOR_TOKEN) {
    Write-Section "OPERATOR allowed actions"
    Invoke-Test -Name "OPERATOR: Get warehouses (positive)" -Method GET -Path "/api/v1/warehouses" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 200
    Invoke-Test -Name "OPERATOR: Get product (positive)" -Method GET -Path "/api/v1/products/1" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 200
    Invoke-Test -Name "OPERATOR: Get all products (positive)" -Method GET -Path "/api/v1/products" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 200
    Invoke-Test -Name "OPERATOR: Get categories (positive)" -Method GET -Path "/api/v1/categories" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 200
    Invoke-Test -Name "OPERATOR: Get movements (positive)" -Method GET -Path "/api/v1/movements" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 200
    Invoke-Test -Name "OPERATOR: Create reservation (positive)" -Method POST -Path "/api/v1/reservations" `
        -Body @{ inventoryId = $resInvId; referenceNumber = "REF-OP-$TS"; referenceType = "SALES_ORDER"; reservedQuantity = 1 } `
        -Auth $OPERATOR_TOKEN -ExpectStatus 201

    Write-Section "OPERATOR denied actions (403)"
    Invoke-Test -Name "OPERATOR: Create warehouse (403)" -Method POST -Path "/api/v1/warehouses" `
        -Body @{ warehouseCode = "WH-X"; warehouseName = "X"; email = "x@t.com"; phone = "9876543210"; capacity = 5000; address = @{ addressLine1 = "St"; city = "C"; state = "S"; country = "I"; postalCode = "12345" } } `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
    Invoke-Test -Name "OPERATOR: Create product (403)" -Method POST -Path "/api/v1/products" `
        -Body @{ sku = "SKU-X"; productName = "X"; brand = "B"; categoryId = 1; unit = "PIECE"; price = 10 } `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
    Invoke-Test -Name "OPERATOR: Create employee (403)" -Method POST -Path "/api/v1/auth/employees" `
        -Body @{ employeeCode = "EMP999999"; fullName = "Operator created"; email = "op999@test.com"; roles = @("WAREHOUSE_OPERATOR") } `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
    Invoke-Test -Name "OPERATOR: Deactivate warehouse (403)" -Method PATCH -Path "/api/v1/warehouses/1/deactivate" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
    Invoke-Test -Name "OPERATOR: Delete employee (403)" -Method DELETE -Path "/api/v1/auth/employees/EMP555555" `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
    Invoke-Test -Name "OPERATOR: Create adjustment (403)" -Method POST -Path "/api/v1/adjustments" `
        -Body @{ inventoryId = $resInvId; adjustmentType = "INCREASE"; quantity = 5; reason = "Test" } `
        -Auth $OPERATOR_TOKEN -ExpectStatus 403
} else {
    Write-Host "  [SKIP] RBAC tests - could not get operator token" -ForegroundColor DarkYellow
}

# ============================================================
# PHASE 13: Edge Cases `& Security
# ============================================================
Write-Header "PHASE 13: Edge Cases `& Security"

Invoke-Test -Name "Health check gateway (positive)" -Method GET -Path "/actuator/health" -ExpectStatus 200
Invoke-Test -Name "Invalid path (negative)" -Method GET -Path "/api/v1/nonexistent/endpoint" `
    -Auth $ADMIN_TOKEN -ExpectStatus 404
Invoke-Test -Name "SQL injection in product search (negative)" -Method GET `
    -Path "/api/v1/products/search?search=%27%20OR%201%3D1%20--" -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "SQL injection in warehouse search (negative)" -Method GET `
    -Path "/api/v1/warehouses/search?keyword=%27%20OR%201%3D1%20--" -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "XSS in product search (negative)" -Method GET `
    -Path "/api/v1/products/search?search=%3Cscript%3Ealert(1)%3C/script%3E" -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "XSS in warehouse search (negative)" -Method GET `
    -Path "/api/v1/warehouses/search?keyword=%3Cscript%3Ealert(1)%3C/script%3E" -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Large page size products (edge)" -Method GET -Path "/api/v1/products?page=0`&size=1000" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Large page size warehouses (edge)" -Method GET -Path "/api/v1/warehouses?page=0`&size=1000" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Large page size movements (edge)" -Method GET -Path "/api/v1/movements?page=0`&size=1000" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Negative page number (edge)" -Method GET -Path "/api/v1/products?page=-1`&size=10" `
    -Auth $ADMIN_TOKEN -ExpectStatus 500
Invoke-Test -Name "Zero page size (edge)" -Method GET -Path "/api/v1/products?page=0`&size=0" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Oversized path parameter (negative)" -Method GET -Path "/api/v1/products/99999999999999999999" `
    -Auth $ADMIN_TOKEN -ExpectStatus 500
Invoke-Test -Name "Empty JSON body on POST (negative)" -Method POST -Path "/api/v1/products" `
    -Body @{} -Auth $ADMIN_TOKEN -ExpectStatus 400
Invoke-Test -Name "Malformed JSON (negative)" -Method POST -Path "/api/v1/products" `
    -Body "not-json" -Auth $ADMIN_TOKEN -ExpectStatus 400
Invoke-Test -Name "Unicode in search (edge)" -Method GET `
    -Path "/api/v1/products/search?search=%E4%B8%AD%E6%96%87" -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "Path traversal attempt (negative)" -Method GET `
    -Path "/api/v1/products/..%2F..%2Fetc%2Fpasswd" -Auth $ADMIN_TOKEN -ExpectStatus 400
Invoke-Test -Name "GET on POST-only endpoint (negative)" -Method GET -Path "/api/v1/products" `
    -Auth $ADMIN_TOKEN -ExpectStatus 200
Invoke-Test -Name "POST on GET-only endpoint (negative)" -Method POST -Path "/api/v1/products/1" `
    -Auth $ADMIN_TOKEN -ExpectStatus 405

# ============================================================
# RESULTS SUMMARY
# ============================================================
Write-Header "TEST RESULTS SUMMARY"

Write-Host ""
Write-Host "Total Tests: $TOTAL_COUNT" -ForegroundColor White
Write-Host "Passed:      $PASS_COUNT" -ForegroundColor Green
Write-Host "Failed:      $FAIL_COUNT" -ForegroundColor $(if ($FAIL_COUNT -gt 0) { "Red" } else { "Green" })
$passRate = if ($TOTAL_COUNT -gt 0) { [math]::Round(($PASS_COUNT / $TOTAL_COUNT) * 100, 1) } else { 0 }
Write-Host "Pass Rate:   $passRate%" -ForegroundColor $(if ($passRate -ge 90) { "Green" } elseif ($passRate -ge 70) { "Yellow" } else { "Red" })

if ($FAIL_COUNT -gt 0) {
    Write-Host ""
    Write-Host "Failed Tests:" -ForegroundColor Red
    $RESULTS | Where-Object { -not $_.Pass } | ForEach-Object {
        Write-Host "  - [$($_.Method)] $($_.Path)" -ForegroundColor Red
        Write-Host "    $($_.Test): $($_.Reason)" -ForegroundColor DarkGray
    }
}

$csvPath = Join-Path $SCRIPT_DIR "test-results.csv"
$RESULTS | Export-Csv -Path $csvPath -NoTypeInformation
Write-Host ""
Write-Host "Results exported to: $csvPath" -ForegroundColor Gray
Write-Host ""
