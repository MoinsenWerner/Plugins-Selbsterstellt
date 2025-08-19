#!/bin/bash
set -e

# Script to build the Living plugin on Debian based systems.
# It installs required dependencies (OpenJDK and Maven) and
# then compiles the plugin using Maven.

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

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

for module in living chrono nations; do
    if [ -d "$SCRIPT_DIR/$module" ]; then
        cd "$SCRIPT_DIR/$module"
        mvn -B package
        cd "$SCRIPT_DIR"
    fi
done

echo "Build completed. Plugin jars can be found in each module's target directory."