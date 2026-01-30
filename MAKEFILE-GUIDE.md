# Makefile Quick Reference Guide

This guide provides quick reference for common development tasks using the Makefile.

## Quick Start

```bash
# Show all available commands
make help

# Initial setup (first time only)
make setup

# Build everything
make build

# Run both backend and UI
make run

# Check if services are running
make health
```

## Common Workflows

### Development

```bash
# Start fresh development environment
make dev-fresh              # Clean, setup, build, and run

# Normal development start
make dev                    # Clean, build, and run

# Run just backend
make run-backend            # Starts on http://localhost:8080

# Run just UI
make run-ui                 # Starts on http://localhost:3000

# Run with database
make run-all                # Starts PostgreSQL, backend, and UI
```

### Building

```bash
# Build everything
make build                  # Build backend and UI

# Build separately
make build-backend          # Maven build
make build-ui               # Next.js production build

# Just compile (no packaging)
make compile-backend        # Faster for syntax checking
```

### Testing

```bash
# Run all tests
make test

# Run backend tests
make test-backend

# Run UI tests
make test-ui

# Lint and type check UI
make lint-ui
make type-check-ui
```

### Database

```bash
# Start PostgreSQL (via Docker)
make db-up

# Stop PostgreSQL
make db-down

# Clean database (removes data)
make db-clean

# View database logs
make db-logs

# Open PostgreSQL shell
make db-shell

# H2 Console (dev mode)
make h2-console             # Shows connection info
```

### Stopping Services

```bash
# Stop all running services
make stop

# Stop specific service
make stop-backend
make stop-ui

# Stop everything including database
make stop-all
```

### Cleaning

```bash
# Clean build artifacts
make clean

# Clean everything including database
make clean-all

# Clean specific components
make clean-backend
make clean-ui
make clean-data             # Remove H2 database and thumbnails
```

### Monitoring & Debugging

```bash
# Check service health
make health                 # Shows status of all services
make status                 # Alias for health

# Show running processes
make ps

# Show ports in use
make ports

# View logs
make logs-backend
make logs-ui
```

### API Testing

```bash
# Test API endpoints
make api-test-drives        # List all drives
make api-test-create-drive  # Create a test drive

# Show API documentation
make api-docs               # List available endpoints
```

### Production

```bash
# Build for production
make prod-build

# Run in production mode
make prod-run
```

### Information

```bash
# Show project information
make info

# Show version information
make version

# Show environment variables
make env
```

## Common Development Scenarios

### Starting the Day

```bash
# Option 1: Use existing build
make run

# Option 2: Fresh start with latest changes
make dev

# Option 3: Complete clean slate
make dev-fresh
```

### Making Backend Changes

```bash
# 1. Make your changes
# 2. Stop backend
make stop-backend

# 3. Build and run
make build-backend && make run-backend

# Or in one step
make compile-backend && make run-backend
```

### Making UI Changes

```bash
# UI has hot reload, just save files
# If you need to restart:
make stop-ui && make run-ui
```

### Running Tests Before Commit

```bash
# Run all tests
make test

# Or separately
make test-backend
make test-ui
make lint-ui
```

### Cleaning Up Before Going Home

```bash
# Stop everything
make stop-all

# Or if you want to free up space
make clean-all
```

### Troubleshooting

```bash
# Check what's running
make health
make ps

# Check ports
make ports

# View logs
make logs-backend
make logs-ui

# Nuclear option - clean and restart
make stop-all
make clean-all
make dev-fresh
```

## Tips & Best Practices

1. **Always check health first**: `make health` before starting work
2. **Use `make dev` for clean rebuilds**: When things seem broken
3. **Stop services when not in use**: `make stop-all` to free resources
4. **Use `make help`**: When you forget a command
5. **Test before committing**: `make test` to catch issues early
6. **Clean periodically**: `make clean` to remove stale artifacts

## Keyboard Shortcuts Recommendation

Add these to your `~/.bashrc` or `~/.zshrc`:

```bash
# Picture Model aliases
alias pmh='make health'
alias pmr='make run'
alias pms='make stop'
alias pmb='make build'
alias pmt='make test'
alias pmd='make dev'
alias pmc='make clean'
```

Then you can use:
```bash
pmr   # make run
pmh   # make health
pms   # make stop
```

## Environment Setup

The Makefile automatically sets:
- `JAVA_HOME` to Java 21
- Correct working directories for all commands
- Color-coded output for better readability

## Parallel Execution

Some targets run in parallel for better performance:
- `make run` - Runs backend and UI simultaneously
- `make test` - Runs backend and UI tests in parallel

## Exit Codes

All commands return proper exit codes:
- `0` = Success
- Non-zero = Failure

This allows for scripting:
```bash
make build && make test && make run
```

## Getting Help

```bash
# Show all commands with descriptions
make help

# Show project information
make info

# Show version information
make version
```

## What's Running?

Quick reference for default ports:

| Service    | Port | URL                          | Profile |
|------------|------|------------------------------|---------|
| Backend    | 8080 | http://localhost:8080        | dev     |
| Frontend   | 3000 | http://localhost:3000        | dev     |
| PostgreSQL | 5432 | jdbc:postgresql://localhost  | prod    |
| H2 Console | 8080 | http://localhost:8080/h2-console | dev |

## Troubleshooting Common Issues

### Port Already in Use

```bash
# Check what's using the port
make ports

# Stop all services
make stop-all
```

### Build Fails

```bash
# Clean and rebuild
make clean
make build
```

### Services Won't Start

```bash
# Check health
make health

# Check processes
make ps

# Nuclear option
make stop-all
make clean-all
make setup
make build
make run
```

### Database Connection Issues

```bash
# For dev (H2):
make h2-console  # Shows connection info

# For prod (PostgreSQL):
make db-up       # Ensure database is running
make db-logs     # Check logs
```

## Advanced Usage

### Running Specific Maven Phases

```bash
cd api
mvn clean verify              # Run all tests including integration
mvn spring-boot:run -X        # Debug mode
```

### Running Specific npm Scripts

```bash
cd ui
npm run lint                  # Lint only
npm run build                 # Production build
npm run type-check            # Type checking only
```

### Custom Profiles

```bash
# Backend with custom profile
cd api
mvn spring-boot:run -Dspring-boot.run.profiles=custom

# Or modify the Makefile target
```

## Contributing

When adding new Makefile targets:
1. Add a `## Description` comment after the target
2. Use `@echo` with color codes for output
3. Add proper error handling
4. Update this guide

## Support

For issues or questions:
1. Check `make help`
2. Check `make info`
3. Check this guide
4. Check project README.md
