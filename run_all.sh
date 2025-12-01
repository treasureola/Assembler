#!/bin/bash

set -e

echo "=== Basic Machine Project: Build + Test + Simulator Launch ==="

# 0) JavaFX setup: use existing JAVAFX_HOME if set, otherwise try to auto-detect
if [ -z "$JAVAFX_HOME" ]; then
  # Try a common default path for your machine
  if [ -d "$HOME/Downloads/javafx-sdk-25" ]; then
    export JAVAFX_HOME="$HOME/Downloads/javafx-sdk-25"
    echo "[INFO] Auto-detected JAVAFX_HOME=$JAVAFX_HOME"
  else
    echo "[ERROR] JAVAFX_HOME is not set and auto-detect failed."
    echo "        Please set it manually, e.g.:"
    echo "        export JAVAFX_HOME=\$HOME/Downloads/javafx-sdk-25"
    exit 1
  fi
fi

echo "[1/4] Cleaning previous build..."
rm -rf out
mkdir -p out

echo "[INFO] Using JAVAFX_HOME=$JAVAFX_HOME"

# 2) Compile Java sources WITH JavaFX on the module-path
echo "[2/4] Compiling Java sources..."
javac \
  --module-path "$JAVAFX_HOME/lib" \
  --add-modules javafx.controls,javafx.fxml \
  -d out \
  $(find src -name "*.java")

echo "[2.5/4] Copying UI resource files (FXML)..."
mkdir -p out/BasicMachine/simulator/ui
cp src/BasicMachine/simulator/ui/simulator.fxml out/BasicMachine/simulator/ui/

# 3) Run TestRunner (InstructionSet_Test + Program_1 in terminal, Program2_test assembled only)
echo "[3/4] Running Assembler + CLI Tests via TestRunner..."
java \
  -cp out \
  BasicMachine.TestRunner

# 4) Launch GUI Simulator (you IPL Program2_test load.ld yourself)
echo "[4/4] Launching GUI Simulator..."
java \
  --module-path "$JAVAFX_HOME/lib" \
  --add-modules javafx.controls,javafx.fxml \
  -cp out \
  BasicMachine.simulator.ui.SimulatorApp
