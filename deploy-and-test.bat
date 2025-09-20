@echo off
REM Comprehensive Deployment and Testing Script for Chat Application (Windows)
REM This script deploys the application and runs integration tests

setlocal enabledelayedexpansion

REM Colors for output (Windows CMD)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Configuration
set "PROJECT_ROOT=%~dp0"
set "DOCKER_COMPOSE_FILE=docker-compose.yml"
set "DOCKER_COMPOSE_PROD_FILE=docker-compose.prod.yml"

REM Logging functions
:log_info
echo [%BLUE%INFO%NC%] %~1
goto :eof

:log_success
echo [%GREEN%SUCCESS%NC%] %~1
goto :eof

:log_warning
echo [%YELLOW%WARNING%NC%] %~1
goto :eof

:log_error
echo [%RED%ERROR%NC%] %~1
goto :eof

REM Function to wait for service to be ready
:wait_for_service
set "service_url=%~1"
set "service_name=%~2"
set "max_attempts=30"
set "attempt=1"

call :log_info "Waiting for %service_name% to be ready at %service_url%..."

:wait_loop
if %attempt% leq %max_attempts% (
    powershell -Command "try { $response = Invoke-WebRequest -Uri '%service_url%' -TimeoutSec 5; exit 0 } catch { exit 1 }" >nul 2>&1
    if !errorlevel! equ 0 (
        call :log_success "%service_name% is ready!"
        goto :eof
    )

    call :log_info "Attempt %attempt%/%max_attempts%: %service_name% not ready yet..."
    timeout /t 10 /nobreak >nul
    set /a "attempt+=1"
    goto wait_loop
)

call :log_error "%service_name% failed to start within expected time"
exit /b 1

REM Function to run health checks
:run_health_checks
call :log_info "Running health checks..."

REM Backend health check
call :wait_for_service "http://localhost:8080/actuator/health" "Backend"
if !errorlevel! neq 0 (
    call :log_error "Backend health check failed"
    exit /b 1
)
call :log_success "Backend health check passed"

REM Frontend health check
call :wait_for_service "http://localhost:3000" "Frontend"
if !errorlevel! neq 0 (
    call :log_error "Frontend health check failed"
    exit /b 1
)
call :log_success "Frontend health check passed"
goto :eof

REM Function to verify database connectivity
:verify_database
call :log_info "Verifying database connectivity..."

powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 10; if ($response.Content -match '\"status\":\"UP\"') { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if !errorlevel! equ 0 (
    call :log_success "Database connectivity verified through health check"
) else (
    call :log_error "Database connectivity check failed"
    exit /b 1
)
goto :eof

REM Function to generate deployment report
:generate_report
call :log_info "Generating deployment report..."

for /f "tokens=2 delims==" %%i in ('wmic os get localdatetime /value') do set datetime=%%i
set "report_file=deployment_report_%datetime:~0,8%_%datetime:~8,6%.txt"

echo ======================================== > "%report_file%"
echo CHAT APPLICATION DEPLOYMENT REPORT >> "%report_file%"
echo ======================================== >> "%report_file%"
echo Deployment Date: %date% %time% >> "%report_file%"
echo Environment: %computername% >> "%report_file%"
echo. >> "%report_file%"

echo SERVICES STATUS: >> "%report_file%"
echo --------------- >> "%report_file%"

REM Check services using port checks
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('localhost', 5432); $tcp.Close(); echo 'postgres:5432 - RUNNING' } catch { echo 'postgres:5432 - NOT RUNNING' }" >> "%report_file%"
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('localhost', 6379); $tcp.Close(); echo 'redis:6379 - RUNNING' } catch { echo 'redis:6379 - NOT RUNNING' }" >> "%report_file%"
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('localhost', 9000); $tcp.Close(); echo 'minio:9000 - RUNNING' } catch { echo 'minio:9000 - NOT RUNNING' }" >> "%report_file%"
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('localhost', 8080); $tcp.Close(); echo 'app:8080 - RUNNING' } catch { echo 'app:8080 - NOT RUNNING' }" >> "%report_file%"
powershell -Command "try { $tcp = New-Object System.Net.Sockets.TcpClient; $tcp.Connect('localhost', 3000); $tcp.Close(); echo 'frontend:3000 - RUNNING' } catch { echo 'frontend:3000 - NOT RUNNING' }" >> "%report_file%"

echo. >> "%report_file%"
echo HEALTH CHECKS: >> "%report_file%"
echo -------------- >> "%report_file%"

powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 10; echo 'Backend Health Check - PASSED' } catch { echo 'Backend Health Check - FAILED' }" >> "%report_file%"
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:3000' -TimeoutSec 10; echo 'Frontend Health Check - PASSED' } catch { echo 'Frontend Health Check - FAILED' }" >> "%report_file%"

echo. >> "%report_file%"
echo CONFIGURATION SUMMARY: >> "%report_file%"
echo ---------------------- >> "%report_file%"
echo Docker Compose: %DOCKER_COMPOSE_FILE% >> "%report_file%"
echo Production Config: %DOCKER_COMPOSE_PROD_FILE% >> "%report_file%"
echo CI/CD Pipeline: GitHub Actions >> "%report_file%"
echo Monitoring: Spring Actuator >> "%report_file%"
echo Load Balancing: Nginx >> "%report_file%"
echo Orchestration: Kubernetes manifests ready >> "%report_file%"

call :log_success "Deployment report generated: %report_file%"
goto :eof

REM Main deployment function
:main
call :log_info "Starting Chat Application Deployment and Testing..."
call :log_info "Project Root: %PROJECT_ROOT%"

cd /d "%PROJECT_ROOT%"

REM Pre-deployment checks
call :log_info "Performing pre-deployment checks..."

docker --version >nul 2>&1
if !errorlevel! neq 0 (
    call :log_error "Docker is not installed or not in PATH"
    exit /b 1
)

docker-compose --version >nul 2>&1
if !errorlevel! neq 0 (
    call :log_error "Docker Compose is not installed or not in PATH"
    exit /b 1
)

REM Start services
call :log_info "Starting services with Docker Compose..."
docker-compose -f "%DOCKER_COMPOSE_FILE%" up -d

REM Wait for services to be ready
call :log_info "Waiting for services to initialize..."
timeout /t 30 /nobreak >nul

REM Run health checks
call :run_health_checks
if !errorlevel! neq 0 (
    call :log_error "Health checks failed"
    docker-compose logs
    exit /b 1
)

REM Verify database
call :verify_database
if !errorlevel! neq 0 (
    exit /b 1
)

REM Generate report
call :generate_report

call :log_success "Chat Application deployment completed successfully!"
echo.
call :log_info "Application URLs:"
call :log_info "  Frontend: http://localhost:3000"
call :log_info "  Backend API: http://localhost:8080"
call :log_info "  MinIO Console: http://localhost:9001"
call :log_info "  Health Check: http://localhost:8080/actuator/health"
echo.
call :log_info "To stop the application: docker-compose down"
call :log_info "To view logs: docker-compose logs -f"
goto :eof

REM Cleanup function
:cleanup
call :log_info "Cleaning up..."
cd /d "%PROJECT_ROOT%"
docker-compose down
goto :eof

REM Handle script arguments
if "%1"=="cleanup" (
    call :cleanup
) else if "%1"=="test-only" (
    cd /d "%PROJECT_ROOT%"
    call :run_health_checks
) else (
    call :main
)