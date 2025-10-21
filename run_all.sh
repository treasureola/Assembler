#!/usr/bin/env bash
set -euo pipefail

# ==============================================================
# run_all.sh
# --------------------------------------------------------------
#  Unified build, test, and simulator launcher for the
#  Basic Machine project submission.
#
#  Performs:
#    1. Auto-detects operating system (Mac, Linux, Windows)
#    2. Locates JavaFX SDK automatically (or uses a local folder)
#    3. Compiles all source + test files
#    4. Builds Basic_Machine.jar (self-contained)
#    5. Runs automated tests
#    6. Launches simulator GUI if all tests pass
#
#
# ==============================================================

echo "=== Basic Machine Simulator: Build + Test + Launch ==="
echo ""

# --------------------------------------------------------------
# Step 0 — Detect OS type for JavaFX path
# --------------------------------------------------------------
OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
case "$OS" in
  darwin*)
    DEFAULT_JFX="$HOME/Downloads/javafx-sdk-25/lib" ;;
  linux*)
    DEFAULT_JFX="$HOME/javafx-sdk-25/lib" ;;
  msys*|cygwin*|mingw*)
    DEFAULT_JFX="C:/javafx-sdk-25/lib" ;;
  *)
    DEFAULT_JFX="$HOME/javafx-sdk-25/lib" ;;
esac

JFX="${JFX_PATH:-$DEFAULT_JFX}"   # Allow override via env var
JAR="Basic_Machine.jar"
OUT="out"
MANIFEST="MANIFEST.MF"

# Verify JavaFX directory exists
if [ ! -d "$JFX" ]; then
  echo "⚠️  Warning: JavaFX not found at:"
  echo "   $JFX"
  echo "   Please install or adjust JFX_PATH environment variable."
  echo ""
fi

# ==============================================================
# Step 1 — Clean previous build
# ==============================================================

rm -rf "$OUT" "$JAR" "$MANIFEST"
mkdir -p "$OUT"

# ==============================================================
# Step 2 — Compile all Java source and test files
# ==============================================================

echo "[1/4] Compiling Java sources..."
find src -name "*.java" > sources.txt
find tests -name "*.java" >> sources.txt

javac --module-path "$JFX" \
      --add-modules javafx.controls,javafx.fxml \
      -d "$OUT" @"sources.txt"

# ==============================================================
# Step 3 — Copy UI resource (FXML) into build output
# ==============================================================

mkdir -p "$OUT/BasicMachine/simulator/ui"
cp src/BasicMachine/simulator/ui/simulator.fxml "$OUT/BasicMachine/simulator/ui/"

# ==============================================================
# Step 4 — Create MANIFEST file for the JAR
# ==============================================================

echo "[2/4] Creating MANIFEST..."
cat > "$MANIFEST" <<EOF
Manifest-Version: 1.0
Main-Class: tests.TestRunner
Class-Path: .
EOF

# ==============================================================
# Step 5 — Build self-contained Basic_Machine.jar
# ==============================================================

echo "[3/4] Packaging JAR..."
( cd "$OUT" && jar cfm "../$JAR" "../$MANIFEST" . )

# ==============================================================
# Step 6 — Run the JAR (tests + simulator GUI)
# ==============================================================

echo "[4/4] Running Basic Machine JAR..."
java \
  --module-path "$JFX" \
  --add-modules javafx.controls,javafx.fxml \
  -jar "$JAR"
