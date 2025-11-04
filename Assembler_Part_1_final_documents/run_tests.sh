#!/bin/bash
# ==============================================================
# run_tests.sh
# --------------------------------------------------------------
# Purpose:
#   Automated test runner for the Basic Machine Simulator project.
#   This script compiles the project’s core Java source files and
#   all test files, then executes the consolidated BasicMachine.TestRunner class.
#
# Description:
#   - Compiles all Java source files under src/, excluding GUI (JavaFX) files.
#   - Compiles all automated test classes under tests/.
#   - Executes BasicMachine.TestRunner.java, which runs the four major test cases:
#       1. MemoryTest.java
#       2. CPUInstructionTest.java
#       3. IntegrationTest.java
#       4. SimulatorRunTest.java
#
#   Designed for non-GUI environments (e.g., automated grading servers).
#   This ensures correctness of assembler, loader, memory, and CPU modules
#   without requiring JavaFX or manual input.
#
# Usage:
#   ./run_tests.sh
#
# Output:
#   Each test prints “[PASS]” or “[FAIL]” with details.
#   A summary of total passed and failed tests is shown at the end.
#
# ==============================================================
# Step 1 — Display header
# --------------------------------------------------------------
# Prints the project title and basic information.
# ==============================================================

echo "=== Basic Machine Simulator Automated Test Suite ==="

# ==============================================================
# Step 2 — Compile core source files
# --------------------------------------------------------------
#  - Uses javac to recursively compile all Java files under src/
#    except those in the UI directory (which depend on JavaFX).
#  - Output compiled .class files into the “out/” directory.
# ==============================================================

echo "Compiling core..."
javac -d out $(find src -name "*.java" ! -path "src/BasicMachine/simulator/ui/*")

# ==============================================================
# Step 3 — Compile test files
# --------------------------------------------------------------
#  - Compiles all test files located in the tests/ folder.
#  - Adds both src/ and out/ to the classpath, so the tests
#    can reference already compiled project classes.
#  - The output .class files are placed in out/tests/.
# ==============================================================

echo "Compiling tests..."
javac -cp out:src -d out/tests $(find tests -name "*.java")

# ==============================================================
# Step 4 — Execute consolidated BasicMachine.TestRunner
# --------------------------------------------------------------
#  - Runs the BasicMachine.TestRunner class, which executes all test suites
#    and prints a pass/fail summary.
#  - Uses both the core (out/) and test (out/tests) directories
#    as part of the classpath for dependency resolution.
# ==============================================================

echo "Running test suite..."
java -cp out:out/tests TestRunner
