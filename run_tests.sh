#!/bin/bash

# Basic Machine Simulator - Test Runner
# Testing and Documentation

echo "=== Basic Machine Simulator Test Suite ==="
echo "Testing and Documentation"
echo "=========================================="
echo

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}PASS: $2${NC}"
    else
        echo -e "${RED}FAIL: $2${NC}"
    fi
}

# Function to run a test
run_test() {
    local test_name="$1"
    local test_class="$2"
    local description="$3"
    
    echo -e "${YELLOW}Running $test_name...${NC}"
    echo "Description: $description"
    echo "Command: java -cp out $test_class"
    echo "----------------------------------------"
    
    java -cp out "$test_class" 2>&1
    local exit_code=$?
    
    echo "----------------------------------------"
    print_status $exit_code "$test_name completed"
    echo
}

# Check if out directory exists
if [ ! -d "out" ]; then
    echo -e "${RED}Error: out directory not found. Please compile the project first.${NC}"
    echo "Run: javac -d out src/main/**/*.java"
    exit 1
fi

# Compile test files
echo -e "${YELLOW}Compiling test files...${NC}"
javac -cp out -d out tests/*.java 2>/dev/null
if [ $? -eq 0 ]; then
    print_status 0 "Test compilation successful"
else
    print_status 1 "Test compilation failed"
    exit 1
fi
echo

# Run unit tests
echo -e "${YELLOW}=== UNIT TESTS ===${NC}"
echo

run_test "Memory System Test" "TestMemorySystem" "Tests memory operations, MAR, MBR, and program loading"

run_test "Program Loader Test" "TestLoader" "Tests program loading functionality"

run_test "Final Test Suite" "FinalTestSuite" "Comprehensive test suite for all components"

# Run integration tests if available
if [ -f "tests/TestGUIMemoryIntegration.java" ]; then
    echo -e "${YELLOW}=== INTEGRATION TESTS ===${NC}"
    echo
    
    run_test "GUI Memory Integration Test" "TestGUIMemoryIntegration" "Tests GUI integration with memory system"
    echo
fi

# Test program validation
echo -e "${YELLOW}=== TEST PROGRAMS ===${NC}"
echo

if [ -d "tests" ]; then
    echo "Test programs available:"
    ls -la tests/*.src 2>/dev/null | while read line; do
        echo "  - $line"
    done
    echo
    echo "To use test programs:"
    echo "1. Assemble: Use the assembler to convert .src to .lst files"
    echo "2. Load: Load .lst files into the simulator"
    echo "3. Execute: Run programs in the simulator"
    echo
fi

# Summary
echo -e "${YELLOW}=== TEST SUMMARY ===${NC}"
echo "All tests have been executed."
echo "Check the output above for detailed results."
echo
echo "For more information, see:"
echo "- tests/README.md - Test documentation"
echo "- documentation/architecture/ - System architecture"
echo "- documentation/user/ - User guides"
echo

echo -e "${GREEN}Test execution complete!${NC}"
