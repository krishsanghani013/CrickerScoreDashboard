#!/usr/bin/env bash
set -e

# Create output directories for compiled classes
mkdir -p out
mkdir -p bin

# Compile all Java sources into both output directories so terminal and IDE-style runs stay in sync
javac -d out $(find src -name "*.java")
javac -d bin $(find src -name "*.java")

echo "Build successful. Classes are in ./out and ./bin"
