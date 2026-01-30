#!/bin/bash
# Setup script for Picture Model UI

echo "╔═══════════════════════════════════════════════════════════════════════╗"
echo "║         Picture Model UI - Setup                                     ║"
echo "╚═══════════════════════════════════════════════════════════════════════╝"
echo ""

# Check Node.js version
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 20 LTS or later."
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 20 ]; then
    echo "❌ Node.js version $NODE_VERSION detected. Please upgrade to Node.js 20 LTS or later."
    exit 1
fi

echo "✅ Node.js $(node -v) detected"
echo ""

# Install dependencies
echo "📦 Installing dependencies..."
npm install

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Dependencies installed successfully!"
    echo ""
    echo "╔═══════════════════════════════════════════════════════════════════════╗"
    echo "║         Setup Complete!                                              ║"
    echo "╚═══════════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "To start the development server:"
    echo "  npm run dev"
    echo ""
    echo "The UI will be available at: http://localhost:3000"
    echo ""
    echo "Make sure the backend API is running at: http://localhost:8080"
    echo ""
else
    echo ""
    echo "❌ Failed to install dependencies"
    exit 1
fi
