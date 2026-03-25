#!/usr/bin/env bash
set -e

# Create output directory for compiled classes
mkdir -p out

# Compile all Java sources into the out/ directory
javac -d out $(find src -name "*.java")

echo "Build successful. Classes are in ./out"