#!/usr/bin/env bash
set -euo pipefail

echo "=== Basic Machine: Build + Assemble + Run ==="

# Clean build folder
rm -rf out
mkdir -p out

# 1️⃣ Compile all Java sources (Assembler + Simulator + Modules)
echo "[1/3] Compiling sources..."
javac -d out BasicMachine/Assembler/*.java BasicMachine/CPU_Module/*.java BasicMachine/Memory_Module/*.java BasicMachine/simulator/*.java

# 2️⃣ Assemble the source.src → output.lst + load.ld
echo "[2/3] Running Assembler..."
java -cp out Assembler.Assembler

# 3️⃣ Launch Simulator (if desired)
echo "[3/3] Launching Simulator..."
java -cp out BasicMachine.simulator.MainSimulator  # adjust class name if different
