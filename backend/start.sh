#!/bin/bash
# Dev startup script — used only for local development

# Load environment variables from .env file
echo "Loading backend environment variables from .env ..."
export $(grep -v '^#' .env | xargs)

# Detect whether to use system Maven or Maven Wrapper
if command -v mvn >/dev/null 2>&1; then
    MVN_CMD="mvn"
    echo "Using system-installed Maven: mvn"
elif [ -x "./mvnw" ]; then
    MVN_CMD="./mvnw"
    echo "Using Maven Wrapper: ./mvnw"
else
    echo "No Maven or Maven Wrapper found. Exiting."
    exit 1
fi

# Normal startup with nohup in background
echo "Starting Spring Boot backend in normal mode..."
nohup $MVN_CMD spring-boot:run > backend.log 2>&1 &

# ---------------------------------------------
# Debug mode (uncomment to enable)
# echo "Starting backend with debug & trace flags..."
# nohup $MVN_CMD spring-boot:run -X -Dspring-boot.run.arguments=--debug > trace.log 2>&1 &
# ---------------------------------------------

echo "Backend launch initiated. Logs: backend.log (or trace.log if debug enabled)"
