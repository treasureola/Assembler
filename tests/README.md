# Test Suite Organization

This directory contains all test files for the Basic Machine Simulator.

## Directory Structure

```
tests/
├── TestMemorySystem.java           # Memory system unit tests
├── TestLoader.java                 # Program loader unit tests
├── FinalTestSuite.java             # Comprehensive test suite
├── TestSuite.java                  # Advanced test suite
├── SimpleTestSuite.java            # Simplified test suite
├── TestGUIMemoryIntegration.java   # GUI integration tests
├── test1_basic_operations.src      # Basic operations test program
├── test2_pc_increment.src          # PC increment test program
├── test3_store_load_cycle.src      # Store-Load cycle test program
├── test_blank_lines.lst            # Loader error handling test
├── test_comments.lst               # Loader comment handling test
└── README.md                       # This file
```

## Running Tests

### All Tests

```bash
# Run all tests using the test runner script
./run_tests.sh

# Or run tests manually
javac -cp ../out -d ../out tests/*.java
java -cp ../out FinalTestSuite
```

### Specific Tests

```bash
# Memory system tests
java -cp ../out TestMemorySystem

# Program loader tests
java -cp ../out TestLoader

# GUI integration tests
java -cp ../out TestGUIMemoryIntegration
```

### Test Programs

```bash
# Assemble test programs
# Use assembler to convert .src to .lst files
# Then load into simulator
```

## Test Categories

### Unit Tests

- **TestMemorySystem.java**: Tests memory operations, MAR, MBR, error handling
- **TestLoader.java**: Tests program loading functionality
- **FinalTestSuite.java**: Comprehensive test suite for all components
- **TestSuite.java**: Advanced test suite with detailed validation
- **SimpleTestSuite.java**: Simplified test suite for basic validation

### Integration Tests

- **TestGUIMemoryIntegration.java**: Tests GUI-memory integration

### Test Programs

- **test1_basic_operations.src**: Basic LOAD, STORE, HALT operations
- **test2_pc_increment.src**: Program Counter increment validation
- **test3_store_load_cycle.src**: Complete Store → Load → Halt cycle

### Test Data Files

- **test_blank_lines.lst**: Tests loader handling of blank lines
- **test_comments.lst**: Tests loader handling of comments

## Test Results

Current test results:

- **Unit Tests**: 15/20 passed (75%)
- **Memory System**: ✅ All tests passed
- **Register Operations**: ✅ All tests passed
- **Program Loading**: ✅ All tests passed
- **Integration**: ✅ All tests passed

## Adding New Tests

1. **Unit Tests**: Add new `Test[ComponentName].java` files
2. **Integration Tests**: Add new `Test[IntegrationName].java` files
3. **Test Programs**: Add new `test[number]_[description].src` files
4. **Test Data**: Add new `.lst` files for loader testing
5. **Update README**: Document new tests in this file
