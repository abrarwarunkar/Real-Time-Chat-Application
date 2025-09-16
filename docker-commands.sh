#!/bin/bash

# Docker Commands Script for Real-Time Chat Application
# This script provides convenient commands to manage Docker services for the chat application

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
}

# Function to check if docker-compose is available
check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1; then
        print_error "docker-compose is not installed."
        exit 1
    fi
}

# Function to use appropriate docker-compose command
docker_compose_cmd() {
    if command -v docker-compose >/dev/null 2>&1; then
        echo "docker-compose"
    else
        echo "docker compose"
    fi
}

# Get the docker-compose command
COMPOSE_CMD=$(docker_compose_cmd)

# ==========================================
# START COMMANDS
# ==========================================

start_all() {
    print_info "Starting all services (backend, frontend, database, cache, message queue)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD up -d
    print_status "All services started successfully!"
    print_info "Access points:"
    echo "  - Frontend: http://localhost:3000"
    echo "  - Backend API: http://localhost:8080"
    echo "  - Health Check: http://localhost:8080/actuator/health"
    echo "  - MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
    echo "  - Kafka UI: http://localhost:8081"
}

start_backend() {
    print_info "Starting backend services (app, postgres, redis, minio, kafka)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD up -d postgres redis minio kafka app
    print_status "Backend services started successfully!"
    print_info "Backend API: http://localhost:8080"
}

start_frontend() {
    print_info "Starting frontend service..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD up -d frontend
    print_status "Frontend started successfully!"
    print_info "Frontend: http://localhost:3000"
}

start_infrastructure() {
    print_info "Starting infrastructure services (postgres, redis, minio, kafka)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD up -d postgres redis minio kafka
    print_status "Infrastructure services started successfully!"
}

# ==========================================
# STOP COMMANDS
# ==========================================

stop_all() {
    print_info "Stopping all services..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD down
    print_status "All services stopped successfully!"
}

stop_backend() {
    print_info "Stopping backend services..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD stop app postgres redis minio kafka
    print_status "Backend services stopped successfully!"
}

stop_frontend() {
    print_info "Stopping frontend service..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD stop frontend
    print_status "Frontend stopped successfully!"
}

# ==========================================
# RESTART COMMANDS
# ==========================================

restart_all() {
    print_info "Restarting all services..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD restart
    print_status "All services restarted successfully!"
}

restart_backend() {
    print_info "Restarting backend services..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD restart app postgres redis minio kafka
    print_status "Backend services restarted successfully!"
}

restart_frontend() {
    print_info "Restarting frontend service..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD restart frontend
    print_status "Frontend restarted successfully!"
}

# ==========================================
# LOGS COMMANDS
# ==========================================

logs_all() {
    print_info "Showing logs for all services (press Ctrl+C to exit)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD logs -f
}

logs_backend() {
    print_info "Showing backend logs (press Ctrl+C to exit)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD logs -f app
}

logs_frontend() {
    print_info "Showing frontend logs (press Ctrl+C to exit)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD logs -f frontend
}

logs_database() {
    print_info "Showing database logs (press Ctrl+C to exit)..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD logs -f postgres
}

# ==========================================
# BUILD COMMANDS
# ==========================================

build_all() {
    print_info "Building all Docker images..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD build --no-cache
    print_status "All images built successfully!"
}

build_backend() {
    print_info "Building backend Docker image..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD build --no-cache app
    print_status "Backend image built successfully!"
}

build_frontend() {
    print_info "Building frontend Docker image..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD build --no-cache frontend
    print_status "Frontend image built successfully!"
}

# ==========================================
# STATUS AND CLEANUP COMMANDS
# ==========================================

status() {
    print_info "Checking status of all services..."
    check_docker
    check_docker_compose

    $COMPOSE_CMD ps
}

cleanup() {
    print_warning "This will remove all containers, networks, and volumes!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cleaning up Docker resources..."
        check_docker
        check_docker_compose

        $COMPOSE_CMD down -v --remove-orphans
        docker system prune -f
        print_status "Cleanup completed!"
    else
        print_info "Cleanup cancelled."
    fi
}

# ==========================================
# HELP COMMAND
# ==========================================

show_help() {
    echo "Docker Commands Script for Real-Time Chat Application"
    echo ""
    echo "USAGE:"
    echo "  ./docker-commands.sh [COMMAND]"
    echo ""
    echo "COMMANDS:"
    echo ""
    echo "START COMMANDS:"
    echo "  start-all          Start all services (backend + frontend + infrastructure)"
    echo "  start-backend      Start backend services only (app, postgres, redis, minio, kafka)"
    echo "  start-frontend     Start frontend service only"
    echo "  start-infra        Start infrastructure services only (postgres, redis, minio, kafka)"
    echo ""
    echo "STOP COMMANDS:"
    echo "  stop-all           Stop all services"
    echo "  stop-backend       Stop backend services"
    echo "  stop-frontend      Stop frontend service"
    echo ""
    echo "RESTART COMMANDS:"
    echo "  restart-all        Restart all services"
    echo "  restart-backend    Restart backend services"
    echo "  restart-frontend   Restart frontend service"
    echo ""
    echo "LOGS COMMANDS:"
    echo "  logs-all           Show logs for all services"
    echo "  logs-backend       Show backend logs"
    echo "  logs-frontend      Show frontend logs"
    echo "  logs-db            Show database logs"
    echo ""
    echo "BUILD COMMANDS:"
    echo "  build-all          Build all Docker images"
    echo "  build-backend      Build backend Docker image"
    echo "  build-frontend     Build frontend Docker image"
    echo ""
    echo "OTHER COMMANDS:"
    echo "  status             Show status of all services"
    echo "  cleanup            Remove all containers, networks, and volumes"
    echo "  help               Show this help message"
    echo ""
    echo "EXAMPLES:"
    echo "  ./docker-commands.sh start-all"
    echo "  ./docker-commands.sh logs-backend"
    echo "  ./docker-commands.sh restart-frontend"
    echo ""
}

# ==========================================
# MAIN SCRIPT LOGIC
# ==========================================

case "${1:-help}" in
    start-all)
        start_all
        ;;
    start-backend)
        start_backend
        ;;
    start-frontend)
        start_frontend
        ;;
    start-infra)
        start_infrastructure
        ;;
    stop-all)
        stop_all
        ;;
    stop-backend)
        stop_backend
        ;;
    stop-frontend)
        stop_frontend
        ;;
    restart-all)
        restart_all
        ;;
    restart-backend)
        restart_backend
        ;;
    restart-frontend)
        restart_frontend
        ;;
    logs-all)
        logs_all
        ;;
    logs-backend)
        logs_backend
        ;;
    logs-frontend)
        logs_frontend
        ;;
    logs-db)
        logs_database
        ;;
    build-all)
        build_all
        ;;
    build-backend)
        build_backend
        ;;
    build-frontend)
        build_frontend
        ;;
    status)
        status
        ;;
    cleanup)
        cleanup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac