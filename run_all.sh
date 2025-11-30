#!/bin/bash
set -euo pipefail

# ============================================================
# Basic Machine Project: Build + Program 2 End-to-End Demo
# ------------------------------------------------------------
# What this script does:
#   1. Cleans and recreates ./out
#   2. Compiles ALL Java sources (CPU + Assembler + GUI)
#   3. Copies JavaFX FXML resources into out/
#   4. Runs BasicMachine.TestRunner, which:
#        - Runs Assembler on Program2_test.src
#        - Runs text-mode simulator
#        - Launches JavaFX GUI
#
# IMPORTANT:
#   Assembler writes:
#       <PROJECT_ROOT>/output.lst
#       <PROJECT_ROOT>/load.ld
#   where PROJECT_ROOT is the directory containing this script.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

SRC_DIR="$SCRIPT_DIR/src"
OUT_DIR="$SCRIPT_DIR/out"

echo "=== Basic Machine Project: Build + Program 2 Demo ==="

# ------------------------------------------------------------
# 1) Clean and recreate out/
# ------------------------------------------------------------
echo "[1/4] Cleaning previous build..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# ------------------------------------------------------------
# 2) Locate JavaFX SDK
#    Default: $HOME/Downloads/javafx-sdk-25
# ------------------------------------------------------------
if [ -z "${JAVAFX_HOME:-}" ]; then
  JAVAFX_HOME="$HOME/Downloads/javafx-sdk-25"
fi

if [ ! -d "$JAVAFX_HOME" ] || [ ! -d "$JAVAFX_HOME/lib" ]; then
  echo "ERROR: Could not find a valid JavaFX SDK."
  echo "Tried: $JAVAFX_HOME"
  echo
  echo "Make sure JavaFX is installed and either:"
  echo "  1) It is at: \$HOME/Downloads/javafx-sdk-25"
  echo "     (current default), OR"
  echo "  2) You set JAVAFX_HOME manually, e.g.:"
  echo "       export JAVAFX_HOME=\$HOME/Downloads/javafx-sdk-25"
  exit 1
fi

echo "[INFO] Using JAVAFX_HOME=$JAVAFX_HOME"

# ------------------------------------------------------------
# 3) Compile all Java sources (CPU + Assembler + GUI)
# ------------------------------------------------------------
echo "[2/4] Compiling Java sources..."

find "$SRC_DIR" -name "*.java" > "$SCRIPT_DIR/sources.txt"

javac \
  --module-path "$JAVAFX_HOME/lib" \
  --add-modules javafx.controls,javafx.fxml \
  -d "$OUT_DIR" \
  @"$SCRIPT_DIR/sources.txt"

echo "[2/4] Compilation successful."

# ------------------------------------------------------------
# 2.5) Copy UI resource files (FXML) into out/
# ------------------------------------------------------------
echo "[2.5/4] Copying UI resource files (FXML)..."
UI_SRC_DIR="$SRC_DIR/BasicMachine/simulator/ui"
UI_DST_DIR="$OUT_DIR/BasicMachine/simulator/ui"

if [ -d "$UI_SRC_DIR" ]; then
  mkdir -p "$UI_DST_DIR"
  cp -R "$UI_SRC_DIR/"* "$UI_DST_DIR/"
else
  echo "[WARN] UI source directory not found: $UI_SRC_DIR"
fi

# ------------------------------------------------------------
# 4) Run TestRunner (Assembler → Simulator → GUI)
# ------------------------------------------------------------
echo "[3/4] Running Assembler + Simulators via TestRunner..."

cd "$OUT_DIR"

java \
  --module-path "$JAVAFX_HOME/lib" \
  --add-modules javafx.controls,javafx.fxml \
  BasicMachine.TestRunner

echo "[4/4] Finished. If JavaFX GUI didn't appear, check console output."
