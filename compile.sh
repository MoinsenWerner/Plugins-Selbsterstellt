#!/bin/bash
set -e

# Script to build a plugin on Debian based systems.
# Usage: ./compile.sh [plugin-directory]
# If no directory is provided, it defaults to 'nations'.

PLUGIN_DIR="${1:-nations}"

if ! command -v javac >/dev/null 2>&1; then
    echo "Installing OpenJDK 21..."
    sudo apt-get update
    sudo apt-get install -y openjdk-21-jdk
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "Installing Maven..."
    sudo apt-get update
    sudo apt-get install -y maven
fi

cd "$(dirname "$0")/${PLUGIN_DIR}"

mvn -B package

echo "Build completed. The plugin jar can be found in ${PLUGIN_DIR}/target."
