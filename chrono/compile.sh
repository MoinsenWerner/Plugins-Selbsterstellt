#!/bin/bash
set -euo pipefail

# Install Java 21 if missing
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q '21'; then
  echo "Installing OpenJDK 21..."
  sudo apt-get update
  sudo apt-get install -y openjdk-21-jdk
fi

# Install Maven if missing
if ! command -v mvn >/dev/null 2>&1; then
  echo "Installing Maven..."
  sudo apt-get update
  sudo apt-get install -y maven
fi

# Build the plugin
mvn -B package

echo "Build complete. Jar located in target/"
