# App: Picture Model
# Package: build
# File: Makefile
# Version: 0.1.0
# Turns: 5
# Author: codex
# Date: 2026-01-30T00:39:15Z
# Exports: make targets
# Description: Build, run, database, and utility commands for Picture Model.

# Configuration
SHELL := /bin/bash
.PHONY: help all setup build run stop clean test health logs db-up db-down db-clean db-logs db-shell db-migrate db-seed db-load db-query db-psql

# Directories
API_DIR := api
UI_DIR := ui
DOCKER_DIR := docker
ENV_FILE := .env.postgresql
DB_SERVICE := postgresql-db
DB_MIGRATION := /app/db/migrations/01_picture_model_tables.sql
DB_SEED := /app/db/scripts/picture_model_test_data.sql

# Java Configuration
JAVA_HOME := $(shell /usr/libexec/java_home -v 21)
export JAVA_HOME

# Colors for output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

#=============================================================================
# Help
#=============================================================================

help: ## Show this help message
	@echo "$(BLUE)Picture Model - Available Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

#=============================================================================
# Setup & Installation
#=============================================================================

setup: ## Initial project setup (install dependencies)
	@echo "$(BLUE)Setting up Picture Model...$(NC)"
	@$(MAKE) install-ui
	@echo "$(GREEN)✓ Setup complete!$(NC)"

install-ui: ## Install UI dependencies
	@echo "$(BLUE)Installing UI dependencies...$(NC)"
	@cd $(UI_DIR) && npm install
	@echo "$(GREEN)✓ UI dependencies installed$(NC)"

#=============================================================================
# Build Commands
#=============================================================================

build: build-backend build-ui ## Build both backend and UI

build-backend: ## Build backend (Spring Boot)
	@echo "$(BLUE)Building backend...$(NC)"
	@cd $(API_DIR) && mvn clean package -DskipTests
	@echo "$(GREEN)✓ Backend built successfully$(NC)"

build-ui: ## Build UI for production
	@echo "$(BLUE)Building UI...$(NC)"
	@cd $(UI_DIR) && npm run build
	@echo "$(GREEN)✓ UI built successfully$(NC)"

compile-backend: ## Compile backend without packaging
	@echo "$(BLUE)Compiling backend...$(NC)"
	@cd $(API_DIR) && mvn clean compile
	@echo "$(GREEN)✓ Backend compiled$(NC)"

#=============================================================================
# Run Commands
#=============================================================================

run: ## Run both backend and UI in development mode
	@echo "$(BLUE)Starting Picture Model...$(NC)"
	@$(MAKE) -j2 run-backend run-ui

run-all: db-up ## Start database, backend, and UI
	@echo "$(BLUE)Starting all services...$(NC)"
	@sleep 3
	@$(MAKE) -j2 run-backend run-ui

run-backend: ## Run backend in development mode
	@echo "$(BLUE)Starting backend on http://localhost:8080$(NC)"
	@cd $(API_DIR) && mvn spring-boot:run -Dspring-boot.run.profiles=dev

run-backend-jar: build-backend ## Run backend from JAR file
	@echo "$(BLUE)Starting backend from JAR...$(NC)"
	@cd $(API_DIR) && java -jar target/picture-model-api-2.0.0.jar --spring.profiles.active=dev

run-backend-prod: ## Run backend in production mode
	@echo "$(BLUE)Starting backend (production)...$(NC)"
	@cd $(API_DIR) && java -jar target/picture-model-api-2.0.0.jar --spring.profiles.active=prod

run-ui: ## Run UI in development mode
	@echo "$(BLUE)Starting UI on http://localhost:3000$(NC)"
	@cd $(UI_DIR) && npm run dev

run-ui-prod: build-ui ## Run UI in production mode
	@echo "$(BLUE)Starting UI (production)...$(NC)"
	@cd $(UI_DIR) && npm run start

#=============================================================================
# Database Commands
#=============================================================================

db-up: ## Start PostgreSQL database with Docker Compose
	@echo "$(BLUE)Starting PostgreSQL database...$(NC)"
	@if [ -f docker-compose.yml ]; then \
		docker compose --profile postgresql --env-file $(ENV_FILE) up -d $(DB_SERVICE); \
		echo "$(GREEN)✓ Database started$(NC)"; \
	else \
		echo "$(YELLOW)⚠ docker-compose.yml not found, skipping database startup$(NC)"; \
	fi

db-down: ## Stop PostgreSQL database
	@echo "$(BLUE)Stopping database...$(NC)"
	@if [ -f docker-compose.yml ]; then \
		docker compose --profile postgresql --env-file $(ENV_FILE) down; \
		echo "$(GREEN)✓ Database stopped$(NC)"; \
	else \
		echo "$(YELLOW)⚠ docker-compose.yml not found$(NC)"; \
	fi

db-clean: db-down ## Stop database and remove volumes
	@echo "$(BLUE)Cleaning database...$(NC)"
	@if [ -f docker-compose.yml ]; then \
		docker compose --profile postgresql --env-file $(ENV_FILE) down -v; \
		echo "$(GREEN)✓ Database cleaned$(NC)"; \
	else \
		echo "$(YELLOW)⚠ docker-compose.yml not found$(NC)"; \
	fi

db-logs: ## Show database logs
	@if [ -f docker-compose.yml ]; then \
		docker compose --profile postgresql --env-file $(ENV_FILE) logs -f $(DB_SERVICE); \
	else \
		echo "$(YELLOW)⚠ docker-compose.yml not found$(NC)"; \
	fi

db-shell: ## Open PostgreSQL shell
	@echo "$(BLUE)Opening database shell...$(NC)"
	@docker compose --profile postgresql --env-file $(ENV_FILE) exec $(DB_SERVICE) psql -U $${POSTGRES_USER} -d $${POSTGRES_DB}

db-migrate: ## Apply database migrations
	@echo "$(BLUE)Running migrations...$(NC)"
	@docker compose --profile postgresql --env-file $(ENV_FILE) exec -T $(DB_SERVICE) psql -U $${POSTGRES_USER} -d $${POSTGRES_DB} -f $(DB_MIGRATION)
	@echo "$(GREEN)✓ Migrations applied$(NC)"

db-seed: ## Load test data into the database
	@echo "$(BLUE)Loading test data...$(NC)"
	@docker compose --profile postgresql --env-file $(ENV_FILE) exec -T $(DB_SERVICE) psql -U $${POSTGRES_USER} -d $${POSTGRES_DB} -f $(DB_SEED)
	@echo "$(GREEN)✓ Test data loaded$(NC)"

db-load: db-migrate db-seed ## Run migrations and seed data
	@echo "$(GREEN)✓ Database ready$(NC)"

db-query: ## Run a basic query to verify data
	@echo "$(BLUE)Querying database...$(NC)"
	@docker compose --profile postgresql --env-file $(ENV_FILE) exec -T $(DB_SERVICE) psql -U $${POSTGRES_USER} -d $${POSTGRES_DB} -c "SELECT COUNT(*) AS image_count FROM images;"

db-psql: ## Open a psql session in the container
	@docker compose --profile postgresql --env-file $(ENV_FILE) exec $(DB_SERVICE) psql -U $${POSTGRES_USER} -d $${POSTGRES_DB}

h2-console: ## Open H2 database console (dev mode)
	@echo "$(BLUE)H2 Console available at: http://localhost:8080/h2-console$(NC)"
	@echo "$(YELLOW)JDBC URL: jdbc:h2:file:./data/picturemodel$(NC)"
	@echo "$(YELLOW)Username: sa$(NC)"
	@echo "$(YELLOW)Password: (empty)$(NC)"

#=============================================================================
# Test Commands
#=============================================================================

test: test-backend test-ui ## Run all tests

test-backend: ## Run backend tests
	@echo "$(BLUE)Running backend tests...$(NC)"
	@cd $(API_DIR) && mvn test
	@echo "$(GREEN)✓ Backend tests completed$(NC)"

test-backend-integration: ## Run backend integration tests
	@echo "$(BLUE)Running backend integration tests...$(NC)"
	@cd $(API_DIR) && mvn verify
	@echo "$(GREEN)✓ Integration tests completed$(NC)"

test-ui: ## Run UI tests
	@echo "$(BLUE)Running UI tests...$(NC)"
	@if grep -q '"test":' $(UI_DIR)/package.json; then \
		cd $(UI_DIR) && npm test; \
		echo "$(GREEN)✓ UI tests completed$(NC)"; \
	else \
		echo "$(YELLOW)⚠ No test script found in package.json$(NC)"; \
	fi

test-ui-watch: ## Run UI tests in watch mode
	@echo "$(BLUE)Running UI tests (watch mode)...$(NC)"
	@cd $(UI_DIR) && npm test -- --watch

lint-ui: ## Lint UI code
	@echo "$(BLUE)Linting UI code...$(NC)"
	@cd $(UI_DIR) && npm run lint

type-check-ui: ## Type check UI code
	@echo "$(BLUE)Type checking UI code...$(NC)"
	@cd $(UI_DIR) && npm run type-check

#=============================================================================
# Stop Commands
#=============================================================================

stop: stop-backend stop-ui ## Stop all running services

stop-all: stop db-down ## Stop all services including database

stop-backend: ## Stop backend server
	@echo "$(BLUE)Stopping backend...$(NC)"
	@pkill -f "picture-model-api" || true
	@pkill -f "spring-boot:run" || true
	@lsof -ti:8080 | xargs kill -9 2>/dev/null || true
	@echo "$(GREEN)✓ Backend stopped$(NC)"

stop-ui: ## Stop UI development server
	@echo "$(BLUE)Stopping UI...$(NC)"
	@pkill -f "next dev" || true
	@lsof -ti:3000 | xargs kill -9 2>/dev/null || true
	@echo "$(GREEN)✓ UI stopped$(NC)"

#=============================================================================
# Clean Commands
#=============================================================================

clean: clean-backend clean-ui ## Clean all build artifacts

clean-all: clean db-clean ## Clean everything including database

clean-backend: ## Clean backend build artifacts
	@echo "$(BLUE)Cleaning backend...$(NC)"
	@cd $(API_DIR) && mvn clean
	@rm -rf $(API_DIR)/data/picturemodel* $(API_DIR)/data/thumbnails
	@echo "$(GREEN)✓ Backend cleaned$(NC)"

clean-ui: ## Clean UI build artifacts
	@echo "$(BLUE)Cleaning UI...$(NC)"
	@rm -rf $(UI_DIR)/.next $(UI_DIR)/out $(UI_DIR)/node_modules/.cache
	@echo "$(GREEN)✓ UI cleaned$(NC)"

clean-data: ## Clean application data (H2 database, thumbnails)
	@echo "$(BLUE)Cleaning application data...$(NC)"
	@rm -rf $(API_DIR)/data
	@echo "$(GREEN)✓ Application data cleaned$(NC)"

#=============================================================================
# Health & Status Commands
#=============================================================================

health: ## Check health of all services
	@echo "$(BLUE)Checking service health...$(NC)"
	@echo ""
	@echo "$(YELLOW)Backend API (http://localhost:8080):$(NC)"
	@curl -s http://localhost:8080/api/drives > /dev/null 2>&1 && \
		echo "  $(GREEN)✓ Running$(NC)" || \
		echo "  $(RED)✗ Not running$(NC)"
	@echo ""
	@echo "$(YELLOW)Frontend UI (http://localhost:3000):$(NC)"
	@curl -s http://localhost:3000 > /dev/null 2>&1 && \
		echo "  $(GREEN)✓ Running$(NC)" || \
		echo "  $(RED)✗ Not running$(NC)"
	@echo ""
	@if [ -f $(API_DIR)/docker-compose.yml ]; then \
		echo "$(YELLOW)PostgreSQL Database:$(NC)"; \
		cd $(API_DIR) && docker-compose ps postgres 2>/dev/null | grep -q "Up" && \
			echo "  $(GREEN)✓ Running$(NC)" || \
			echo "  $(RED)✗ Not running$(NC)"; \
		echo ""; \
	fi

status: health ## Alias for health

ps: ## Show running processes
	@echo "$(BLUE)Running processes:$(NC)"
	@echo ""
	@echo "$(YELLOW)Backend:$(NC)"
	@ps aux | grep -E "picture-model-api|spring-boot:run" | grep -v grep || echo "  Not running"
	@echo ""
	@echo "$(YELLOW)Frontend:$(NC)"
	@ps aux | grep "next dev" | grep -v grep || echo "  Not running"
	@echo ""
	@if [ -f $(API_DIR)/docker-compose.yml ]; then \
		echo "$(YELLOW)Docker containers:$(NC)"; \
		cd $(API_DIR) && docker-compose ps 2>/dev/null || echo "  None"; \
	fi

ports: ## Show ports in use
	@echo "$(BLUE)Ports in use:$(NC)"
	@echo ""
	@echo "$(YELLOW)Port 8080 (Backend):$(NC)"
	@lsof -i :8080 || echo "  Not in use"
	@echo ""
	@echo "$(YELLOW)Port 3000 (Frontend):$(NC)"
	@lsof -i :3000 || echo "  Not in use"
	@echo ""
	@echo "$(YELLOW)Port 5432 (PostgreSQL):$(NC)"
	@lsof -i :5432 || echo "  Not in use"

#=============================================================================
# Logs Commands
#=============================================================================

logs-backend: ## Tail backend logs
	@echo "$(BLUE)Backend logs:$(NC)"
	@tail -f $(API_DIR)/logs/application.log 2>/dev/null || \
		echo "$(YELLOW)No log file found. Backend might be running in console mode.$(NC)"

logs-ui: ## Tail UI logs
	@echo "$(BLUE)UI logs:$(NC)"
	@tail -f /tmp/picture-model-ui.log 2>/dev/null || \
		echo "$(YELLOW)No log file found. UI might be running in console mode.$(NC)"

#=============================================================================
# Development Commands
#=============================================================================

dev: ## Start development environment (clean build + run all)
	@echo "$(BLUE)Starting development environment...$(NC)"
	@$(MAKE) clean
	@$(MAKE) build
	@$(MAKE) run

dev-fresh: clean-all setup db-up ## Fresh development setup (clean everything and start fresh)
	@echo "$(BLUE)Fresh development setup...$(NC)"
	@sleep 3
	@$(MAKE) build
	@$(MAKE) run

watch-ui: ## Run UI in development mode with hot reload
	@$(MAKE) run-ui

watch-backend: ## Run backend with auto-reload (spring-boot-devtools)
	@$(MAKE) run-backend

#=============================================================================
# Production Commands
#=============================================================================

prod-build: ## Build for production
	@echo "$(BLUE)Building for production...$(NC)"
	@$(MAKE) build-backend
	@$(MAKE) build-ui
	@echo "$(GREEN)✓ Production build complete$(NC)"

prod-run: ## Run in production mode
	@echo "$(BLUE)Starting production services...$(NC)"
	@$(MAKE) -j2 run-backend-prod run-ui-prod

#=============================================================================
# API Testing Commands
#=============================================================================

api-test-drives: ## Test drives API endpoint
	@echo "$(BLUE)Testing drives API...$(NC)"
	@curl -s http://localhost:8080/api/drives | jq '.' || \
		echo "$(RED)Failed to connect to API$(NC)"

api-test-create-drive: ## Test creating a drive
	@echo "$(BLUE)Creating test drive...$(NC)"
	@curl -s -X POST http://localhost:8080/api/drives \
		-H "Content-Type: application/json" \
		-d '{"name":"Test Drive","type":"LOCAL","connectionUrl":"/tmp","rootPath":"/","autoConnect":false,"autoCrawl":false}' \
		| jq '.' || echo "$(RED)Failed$(NC)"

api-docs: ## Open API documentation (if available)
	@echo "$(YELLOW)API Base URL: http://localhost:8080/api$(NC)"
	@echo "$(YELLOW)Endpoints:$(NC)"
	@echo "  GET    /api/drives"
	@echo "  POST   /api/drives"
	@echo "  GET    /api/drives/{id}"
	@echo "  PUT    /api/drives/{id}"
	@echo "  DELETE /api/drives/{id}"
	@echo "  POST   /api/drives/{id}/connect"
	@echo "  POST   /api/drives/{id}/disconnect"

#=============================================================================
# Utility Commands
#=============================================================================

version: ## Show version information
	@echo "$(BLUE)Version Information:$(NC)"
	@echo ""
	@echo "$(YELLOW)Java:$(NC)"
	@java -version 2>&1 | head -1
	@echo ""
	@echo "$(YELLOW)Maven:$(NC)"
	@mvn -version 2>&1 | head -1
	@echo ""
	@echo "$(YELLOW)Node:$(NC)"
	@node -v
	@echo ""
	@echo "$(YELLOW)npm:$(NC)"
	@npm -v

env: ## Show environment variables
	@echo "$(BLUE)Environment:$(NC)"
	@echo "JAVA_HOME: $(JAVA_HOME)"
	@echo "API_DIR: $(API_DIR)"
	@echo "UI_DIR: $(UI_DIR)"

info: ## Show project information
	@echo "$(BLUE)Picture Model - Multi-Drive Image Management$(NC)"
	@echo ""
	@echo "$(YELLOW)Project Structure:$(NC)"
	@echo "  api/    - Spring Boot backend"
	@echo "  ui/     - Next.js frontend"
	@echo ""
	@echo "$(YELLOW)URLs:$(NC)"
	@echo "  Backend:  http://localhost:8080"
	@echo "  Frontend: http://localhost:3000"
	@echo "  H2 Console: http://localhost:8080/h2-console"
	@echo ""
	@echo "$(YELLOW)Profiles:$(NC)"
	@echo "  dev  - H2 database, hot reload"
	@echo "  prod - PostgreSQL database"

#=============================================================================
# Default Target
#=============================================================================

.DEFAULT_GOAL := help
