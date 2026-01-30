#!/bin/bash
# Codex Agentic Pipeline - Health Check
# Shows current session context and pipeline status

set -e

echo "╔═══════════════════════════════════════════════════════════════════════╗"
echo "║         CODEX AGENTIC PIPELINE - HEALTH CHECK                        ║"
echo "╚═══════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check functions
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        return 0
    else
        echo -e "${RED}✗${NC} $2 (NOT FOUND: $1)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        return 0
    else
        echo -e "${RED}✗${NC} $2 (NOT FOUND: $1)"
        return 1
    fi
}

# Resolve variables
SANDBOX_BASE_DIRECTORY="/Users/bobware/ai-projects"
AGENTIC_PIPELINE_PROJECT="${SANDBOX_BASE_DIRECTORY}/codex-agentic-ai-pipeline"
TARGET_PROJECT="${SANDBOX_BASE_DIRECTORY}/picture-model"
PROJECT_CONTEXT="${TARGET_PROJECT}/ai/context/project_context.md"

# Get TURN_ID
if [ -f "${TARGET_PROJECT}/ai/agentic-pipeline/turns_index.csv" ]; then
    TURN_ID=$(tail -n +2 "${TARGET_PROJECT}/ai/agentic-pipeline/turns_index.csv" | wc -l | xargs)
    TURN_ID=$((TURN_ID + 1))
else
    TURN_ID=1
fi

CURRENT_TURN_DIRECTORY="${TARGET_PROJECT}/ai/agentic-pipeline/turns/${TURN_ID}"

# Get application pattern
if [ -f "$PROJECT_CONTEXT" ]; then
    ACTIVE_PATTERN_NAME=$(grep "\*\*Pattern Name:\*\*" "$PROJECT_CONTEXT" | sed 's/.*\*\* //' | xargs)
    if [ -z "$ACTIVE_PATTERN_NAME" ]; then
        ACTIVE_PATTERN_NAME="spring-boot-mvc-jpa-postgresql"
    fi
else
    ACTIVE_PATTERN_NAME="spring-boot-mvc-jpa-postgresql"
fi

ACTIVE_PATTERN_PATH="${AGENTIC_PIPELINE_PROJECT}/application-implementation-patterns/${ACTIVE_PATTERN_NAME}"
EXECUTION_PLAN="${ACTIVE_PATTERN_PATH}/execution-plan.md"

echo "═══════════════════════════════════════════════════════════════════════"
echo "SESSION CONTEXT"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
echo -e "${BLUE}Timestamp:${NC}                  $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo -e "${BLUE}TURN_ID:${NC}                    ${TURN_ID}"
echo -e "${BLUE}SANDBOX_BASE_DIRECTORY:${NC}     ${SANDBOX_BASE_DIRECTORY}"
echo -e "${BLUE}AGENTIC_PIPELINE_PROJECT:${NC}   ${AGENTIC_PIPELINE_PROJECT}"
echo -e "${BLUE}TARGET_PROJECT:${NC}             ${TARGET_PROJECT}"
echo -e "${BLUE}ACTIVE_PATTERN_NAME:${NC}        ${ACTIVE_PATTERN_NAME}"
echo -e "${BLUE}CURRENT_TURN_DIRECTORY:${NC}     ${CURRENT_TURN_DIRECTORY}"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "PIPELINE STATUS"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
check_dir "$AGENTIC_PIPELINE_PROJECT" "Agentic Pipeline Project"
check_file "${AGENTIC_PIPELINE_PROJECT}/AGENTS.md" "Pipeline AGENTS.md"
check_dir "$ACTIVE_PATTERN_PATH" "Application Pattern: $ACTIVE_PATTERN_NAME"
check_file "$EXECUTION_PLAN" "Execution Plan"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "PROJECT STATUS"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
check_dir "$TARGET_PROJECT" "Target Project"
check_dir "${TARGET_PROJECT}/ai" "AI Directory"
check_dir "${TARGET_PROJECT}/ai/context" "Context Directory"
check_file "$PROJECT_CONTEXT" "Project Context"
check_dir "${TARGET_PROJECT}/ai/agentic-pipeline" "Agentic Pipeline Directory"
check_dir "${TARGET_PROJECT}/ai/agentic-pipeline/turns" "Turns Directory"
check_file "${TARGET_PROJECT}/ai/agentic-pipeline/turns_index.csv" "Turns Index"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "CURRENT TURN STATUS"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
if [ -d "$CURRENT_TURN_DIRECTORY" ]; then
    echo -e "${GREEN}Turn ${TURN_ID} directory exists${NC}"
    check_file "${CURRENT_TURN_DIRECTORY}/session_context.md" "Session Context"
    check_file "${CURRENT_TURN_DIRECTORY}/adr.md" "Architecture Decision Record"
    check_file "${CURRENT_TURN_DIRECTORY}/pull_request.md" "Pull Request Template"
else
    echo -e "${YELLOW}Turn ${TURN_ID} not started yet${NC}"
fi
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "KEY PATHS"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
echo -e "${BLUE}PROJECT_CONTEXT:${NC}"
echo "  $PROJECT_CONTEXT"
echo ""
echo -e "${BLUE}EXECUTION_PLAN:${NC}"
echo "  $EXECUTION_PLAN"
echo ""
echo -e "${BLUE}CURRENT_TURN_DIRECTORY:${NC}"
echo "  $CURRENT_TURN_DIRECTORY"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo ""
