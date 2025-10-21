#!/usr/bin/env bash
set -euo pipefail

# ==============================================================
# run_all.sh — Final Submission Script
# --------------------------------------------------------------
#  Works in two modes:
#   • Developer Mode — if src/ exists → rebuild + test + run
#   • Grader Mode — if src/ missing → skip rebuild, just test + run
# ==============================================================

echo "=== Basic Machine Simulator: Build + Test + Launch ==="
echo ""

# --------------------------------------------------------------
# Step 0 — Detect OS type and JavaFX path
# --------------------------------------------------------------
OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
case "$OS" in
  darwin*) DEFAULT_JFX="$HOME/Downloads/javafx-sdk-25/lib" ;;
  linux*)  DEFAULT_JFX="$HOME/javafx-sdk-25/lib" ;;
  msys*|cygwin*|mingw*) DEFAULT_JFX="C:/javafx-sdk-25/lib" ;;
  *) DEFAULT_JFX="$HOME/javafx-sdk-25/lib" ;;
esac

JFX="${JFX_PATH:-$DEFAULT_JFX}"
JAR="Basic_Machine.jar"
OUT="out"
MANIFEST="MANIFEST.MF"
SRC_LIST="java_sources.txt"

# --------------------------------------------------------------
# Step 1 — Developer Mode (only if src/ exists)
# --------------------------------------------------------------
if [ -d "src" ]; then
  echo "[DEV MODE] src/ found — rebuilding JAR..."
  rm -rf "$OUT" "$MANIFEST" "$SRC_LIST"
  mkdir -p "$OUT"

  echo "[1/4] Compiling Java sources..."
  find src -name "*.java" > "$SRC_LIST"
  find tests -name "*.java" >> "$SRC_LIST"

  javac --module-path "$JFX" \
        --add-modules javafx.controls,javafx.fxml \
        -d "$OUT" @"$SRC_LIST"

  mkdir -p "$OUT/BasicMachine/simulator/ui"
  if [ -f src/BasicMachine/simulator/ui/simulator.fxml ]; then
    cp src/BasicMachine/simulator/ui/simulator.fxml "$OUT/BasicMachine/simulator/ui/"
  fi

  echo "[2/4] Creating MANIFEST..."
  cat > "$MANIFEST" <<EOF
Manifest-Version: 1.0
Main-Class: tests.TestRunner
Class-Path: .
EOF

  echo "[3/4] Packaging JAR..."
  ( cd "$OUT" && jar cfm "../$JAR" "../$MANIFEST" . )
  echo "✅ Built: $JAR"
else
  echo "[GRADER MODE] src/ not found — using existing $JAR"
fi

# --------------------------------------------------------------
# Step 2 — Sanity check for JAR
# --------------------------------------------------------------
if [ ! -f "$JAR" ]; then
  echo "Error: Basic_Machine.jar not found."
  echo "   Please include it in the submission folder."
  exit 1
fi

# --------------------------------------------------------------
# Step 3 — Compile tests
# --------------------------------------------------------------
echo "[4/4] Compiling tests..."
if [ -d "tests" ]; then
  javac --class-path "$JAR" \
        --module-path "$JFX" \
        --add-modules javafx.controls,javafx.fxml \
        tests/*.java
else
  echo "Error: tests folder not found."
  exit 1
fi

# --------------------------------------------------------------
# Step 4 — Run tests + simulator
# --------------------------------------------------------------
echo ""
echo ">>> Running Basic Machine Automated Tests..."
java --class-path "tests:$JAR" \
     --module-path "$JFX" \
     --add-modules javafx.controls,javafx.fxml \
     tests.TestRunner
