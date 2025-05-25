#!/bin/bash
# Dev stop script — used only for local development

echo "🛑 Gracefully stopping backend and frontend ports..."

# 📥 Ask user for ports to stop
echo "📌 Please enter the ports to stop: backend port first, then frontend port (e.g., 8081 3000)"
read -p "📥 Ports to stop (space-separated): " -a PORTS

for PORT in "${PORTS[@]}"; do
    PIDS=$(lsof -ti tcp:$PORT)

    if [ -z "$PIDS" ]; then
        echo "✅ Port $PORT is already free."
        continue
    fi

    for PID in $PIDS; do
        PROCESS_NAME=$(ps -p $PID -o comm= 2>/dev/null | tail -n 1)

        if [ -z "$PROCESS_NAME" ]; then
            echo "❌ Could not resolve process name for PID $PID — skipping."
            continue
        fi

        echo "⚠️  Port $PORT is used by PID $PID ($PROCESS_NAME)"

        if [[ "$PROCESS_NAME" == "node" || "$PROCESS_NAME" == "java" ]]; then
            echo "🔪 Killing $PROCESS_NAME on port $PORT (PID $PID)..."
            sudo kill -9 $PID
            echo "✅ Port $PORT (PID $PID) is now free."
        else
            echo "❌ Skipping: $PROCESS_NAME is not part of your app."
        fi
    done
done

echo
echo "🎯 All specified ports have been processed."
