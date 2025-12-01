#!/bin/bash
set -e

echo "=== Basic Machine Simulator: Tests + GUI Launch ==="

# ----------------------------------------------------
# Resolve script directory
# ----------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

JAR="$SCRIPT_DIR/Assembler.jar"
JFX_DIR="$SCRIPT_DIR/lib"
JFX_CP="$JFX_DIR/*"

# ----------------------------------------------------
# Sanity checks
# ----------------------------------------------------
if [ ! -f "$JAR" ]; then
  echo "[ERROR] Assembler.jar not found next to run_all.sh."
  echo "        Expected: $JAR"
  exit 1
fi

if [ ! -d "$JFX_DIR" ]; then
  echo "[ERROR] JavaFX libs folder not found."
  echo "        Expected: $JFX_DIR containing javafx-*.jar files"
  exit 1
fi

echo "[INFO] Using JAR      = $JAR"
echo "[INFO] Using JavaFX   = $JFX_DIR"
echo

# ----------------------------------------------------
# 1) Run CLI test suite
# ----------------------------------------------------
echo "[1/2] Running assembler + CPU tests (CLI)..."
java \
  -cp "$JAR:$JFX_CP" \
  BasicMachine.TestRunner

echo
echo "[1/2] CLI tests completed."
echo

# ----------------------------------------------------
# 2) Launch GUI Simulator
# ----------------------------------------------------
echo "[2/2] Launching JavaFX GUI simulator..."
java \
  --module-path "$JFX_DIR" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "$JAR" \
  BasicMachine.simulator.ui.SimulatorApp
