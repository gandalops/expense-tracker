#!/bin/bash
# Dev startup script — used only for local development

echo "🚀 Starting backend and frontend services..."

# 🚀 Start backend
echo "🧭 Starting backend service..."
cd backend || { echo "❌ backend/ folder not found!"; exit 1; }
chmod +x start.sh
./start.sh
echo "⏳ Waiting 5 seconds to ensure backend starts..."
sleep 5

# 🚀 Start frontend
echo "🧭 Starting frontend service..."
cd ../frontend || { echo "❌ frontend/ folder not found!"; exit 1; }
chmod +x start.sh
./start.sh

echo
echo "🎉 All services have been started in the background."
echo "📄 Check backend/backend.log and frontend/frontend.log for details."
