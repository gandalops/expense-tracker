#!/bin/bash
# Dev startup script — used only for local development

# Load environment variables from .env file
echo "Loading frontend environment variables from .env ..."
export $(grep -v '^#' .env | xargs)

# Check if npm is installed
if ! command -v npm >/dev/null 2>&1; then
    echo "npm (Node.js) is not installed or not in PATH. Exiting."
    exit 1
fi

echo "Using system-installed Node/npm"

# Normal React app startup (runs in background with logging)
echo "Starting React frontend in normal mode..."
nohup npm start > frontend.log 2>&1 &

# ---------------------------------------------
# Debug mode (uncomment if you have a debug script set in package.json)
# echo "Starting frontend in debug mode..."
# nohup npm run start:debug > debug.log 2>&1 &
# ---------------------------------------------

echo "Frontend launch initiated. Logs: frontend.log (or debug.log if debug enabled)"
