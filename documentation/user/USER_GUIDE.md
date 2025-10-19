# Basic Machine Simulator - User Guide

## Table of Contents

1. [Getting Started](#getting-started)
2. [GUI Interface](#gui-interface)
3. [Loading Programs](#loading-programs)
4. [Running Programs](#running-programs)
5. [Register Operations](#register-operations)
6. [Memory Operations](#memory-operations)
7. [Troubleshooting](#troubleshooting)
8. [Examples](#examples)

## Getting Started

### Prerequisites

- **Java 11 or higher** (OpenJDK recommended)
- **JavaFX runtime** (included with OpenJDK 11+)
- **Linux/Windows/macOS** operating system

#### Check Java Installation

```bash
java -version
javac -version
```

#### Install JavaFX (if needed)

- **Ubuntu/Debian**: `sudo apt install openjfx`
- **Windows**: Download from [Oracle](https://www.oracle.com/java/) or [OpenJDK](https://openjdk.java.net/)
- **macOS**: `brew install openjdk@11` or download from Oracle

### Complete Setup Instructions

#### 1. Download/Clone the Project

```bash
# If using Git
git clone <repository-url>
cd Assembler

# Or download and extract the ZIP file
# Navigate to the extracted folder
```

#### 2. Compile the Project

```bash
# Compile main source code
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# Compile test suite (optional)
javac -cp out -d out tests/*.java
```

#### 3. Run the Simulator

##### Option A: GUI Simulator (Recommended)

```bash
# Linux
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# Windows (adjust JavaFX path as needed)
java --module-path "C:\Program Files\Java\javafx-11\lib" --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# macOS
java --module-path /opt/homebrew/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

##### Option B: CLI Simulator

```bash
java -cp out BasicMachine.CPU_Module.MainSimulator
```

##### Option C: Assembler Only

```bash
java -cp out Assembler.Assembler
```

#### 4. Run Tests

```bash
# Run all tests
./run_tests.sh

# Or run specific tests
java -cp out TestMemorySystem
java -cp out TestLoader
java -cp out FinalTestSuite
```

### Quick Start Guide

#### Step 1: Open the GUI

Run the GUI simulator using the command above for your platform.

#### Step 2: Load a Program

1. Click the **"Browse"** button
2. Navigate to and select `output.lst` (or any `.lst` file)
3. Click **"IPL"** (Initial Program Load)

#### Step 3: Execute the Program

1. Use **"Step"** to execute one instruction at a time
2. Use **"Run"** to execute continuously
3. Use **"Halt"** to stop execution

#### Step 4: Monitor Registers

- Watch register values change in real-time
- Use blue buttons to copy register values
- Monitor memory operations

### Project Structure

```
Assembler/
├── src/                        # Source code
│   ├── Assembler/              # Two-pass assembler
│   └── BasicMachine/           # Simulator components
├── tests/                      # Test suite
├── documentation/              # Documentation
├── out/                        # Compiled classes
├── load.ld                     # Generated load file
├── output.lst                  # Generated listing file
└── source.src                  # Assembly source
```

## GUI Interface

### Main Window Layout

The simulator GUI is divided into several sections:

#### 1. Register Display Area

- **General Purpose Registers (GPR)**: R0, R1, R2, R3
- **Index Registers (IXR)**: X1, X2, X3
- **Special Registers**: PC, MAR, MBR, IR, CC, MFR
- **Binary LED Display**: 16-bit visual representation

#### 2. Control Panel

- **Browse Button**: Select program files
- **IPL Button**: Initial Program Load
- **Step Button**: Execute one instruction
- **Run Button**: Execute continuously
- **Halt Button**: Stop execution

#### 3. Memory Operations

- **Octal Input Field**: Enter values in octal
- **Load/Store Buttons**: Memory operations
- **Load+/Store+ Buttons**: Post-increment operations

#### 4. Status Area

- **Printer Output**: System messages and console output
- **Console Input**: Text input for programs

## Loading Programs

### Supported File Formats

- **`.lst` files**: Assembly listing files with addresses and machine code
- **`.src` files**: Assembly source files (must be assembled first)

### Loading Process

1. **Click Browse**: Opens file selection dialog
2. **Navigate**: Find your program file
3. **Select**: Choose the `.lst` file
4. **Click IPL**: Loads the program into memory

### File Format Requirements

The `.lst` file should contain lines in the format:

```
<address_octal> <machine_code_octal> <source_line>
```

Example:

```
000006 000012           Data    10
000007 000003           Data    3
000016 102207           LDX     2,7
```

## Running Programs

### Step-by-Step Execution

1. **Load program** using IPL
2. **Click Step** to execute one instruction
3. **Observe changes** in registers and memory
4. **Continue stepping** through the program

### Continuous Execution

1. **Load program** using IPL
2. **Click Run** to execute continuously
3. **Click Halt** to stop execution
4. **Monitor progress** in the printer area

### Program Counter (PC)

- Shows current instruction address
- Increments automatically after each instruction
- Can be set manually for debugging

## Register Operations

### General Purpose Registers (R0-R3)

- **Purpose**: Store operands and results
- **Size**: 16 bits each
- **Display**: Binary format in LED display
- **Operations**: Load, store, arithmetic

### Index Registers (X1-X3)

- **Purpose**: Provide indexed addressing
- **Size**: 16 bits each
- **Display**: Octal format
- **Usage**: Add to memory addresses

### Special Registers

- **PC**: Program Counter (current instruction address)
- **MAR**: Memory Address Register (memory operation address)
- **MBR**: Memory Buffer Register (data being transferred)
- **IR**: Instruction Register (current instruction)
- **CC**: Condition Code (processor status flags)
- **MFR**: Machine Fault Register (error codes)

### Blue Button Operations

1. **Enter value** in register field
2. **Click blue button** next to register
3. **Value appears** in Octal Input field
4. **Use for** Load/Store operations

## Memory Operations

### Load Operations

- **Load**: MBR = Memory[MAR]
- **Load+**: MBR = Memory[MAR], then MAR = MAR + 1

### Store Operations

- **Store**: Memory[MAR] = MBR
- **Store+**: Memory[MAR] = MBR, then MAR = MAR + 1

### Octal Input

- **Format**: 6-digit octal numbers (000000-177777)
- **Usage**: Enter values for memory operations
- **Examples**: 000012, 123456, 177777

## Troubleshooting

### Common Issues and Solutions

#### 1. GUI Won't Start

**Error**: `Error: JavaFX runtime components are missing`

**Solutions**:

```bash
# Install JavaFX
sudo apt install openjfx

# Or use system JavaFX
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

**Other GUI Issues**:

- **Check Java version**: Use Java 11 or higher
- **Check classpath**: Ensure all classes are compiled
- **Check display**: Ensure display is available (for remote connections)

#### 2. Compilation Errors

**Error**: `Could not find or load main class`

**Solutions**:

```bash
# Ensure compilation was successful
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# Check if classes exist
ls -la out/
```

**Error**: `Package does not exist`

**Solutions**:

- Check package declarations in source files
- Ensure all dependencies are compiled
- Verify classpath is correct

#### 3. Program Won't Load

**Error**: `IPL failed: [message]`

**Solutions**:

- **Check file format**: Ensure `.lst` file is correct
- **Check file path**: Verify file exists and is readable
- **Check file content**: Look for proper octal format
- **Try different file**: Use `output.lst` or create new one

#### 4. Program Won't Run

**Error**: `Unknown opcode X, halting`

**Solutions**:

- **Check PC value**: Ensure Program Counter is set correctly
- **Check memory**: Verify program is loaded in memory
- **Check instructions**: Look for illegal opcodes
- **Check addressing**: Ensure memory addresses are correct

#### 5. Registers Not Updating

**Solutions**:

- **Check execution**: Ensure program is actually running
- **Check instruction**: Verify instruction is valid
- **Check addressing**: Ensure memory addresses are correct
- **Try step mode**: Use "Step" instead of "Run"

### Platform-Specific Issues

#### Linux

```bash
# If JavaFX not found
sudo apt install openjfx

# If permission denied
chmod +x run_tests.sh
```

#### Windows

```bash
# Adjust JavaFX path
java --module-path "C:\Program Files\Java\javafx-11\lib" --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# Use forward slashes in paths
java -cp out BasicMachine.CPU_Module.MainSimulator
```

#### macOS

```bash
# Install JavaFX
brew install openjdk@11

# Use correct JavaFX path
java --module-path /opt/homebrew/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

### Error Messages Reference

#### "IPL failed: [message]"

- **Cause**: File not found or invalid format
- **Solution**: Check file path and content

#### "Unknown opcode X, halting"

- **Cause**: Invalid instruction encountered
- **Solution**: Check program for errors

#### "Memory read/write error: invalid address X"

- **Cause**: Address outside valid range (0-2047)
- **Solution**: Check addressing in program

#### "Could not find or load main class"

- **Cause**: Compilation failed or classpath incorrect
- **Solution**: Recompile and check classpath

#### "Error: JavaFX runtime components are missing"

- **Cause**: JavaFX not installed or not in module path
- **Solution**: Install JavaFX or adjust module path

### Debugging Tips

#### 1. Check Compilation

```bash
# Compile with verbose output
javac -verbose -d out src/**/*.java
```

#### 2. Check Classpath

```bash
# List all classes
find out -name "*.class" | head -10
```

#### 3. Check JavaFX

```bash
# Test JavaFX installation
java --list-modules | grep javafx
```

#### 4. Run Tests

```bash
# Run test suite to verify functionality
./run_tests.sh
```

#### 5. Check Logs

- Look for error messages in terminal output
- Check GUI console for status messages
- Verify file permissions and paths

## Complete Example Workflow

### Example 1: Running the Simulator from Scratch

```bash
# 1. Compile the project
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# 2. Start the GUI
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# 3. In the GUI:
#    - Click "Browse"
#    - Select "output.lst"
#    - Click "IPL"
#    - Click "Step" to execute one instruction
#    - Click "Run" to execute continuously
```

### Example 2: Using the CLI Simulator

```bash
# 1. Compile the project
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# 2. Run CLI simulator
java -cp out BasicMachine.CPU_Module.MainSimulator

# Output will show:
# - Register states in octal
# - Memory operations
# - Program execution until halt
```

### Example 3: Running Tests

```bash
# 1. Compile everything
javac -d out src/**/*.java
javac -cp out -d out tests/*.java

# 2. Run all tests
./run_tests.sh

# 3. Or run specific tests
java -cp out TestMemorySystem
java -cp out FinalTestSuite
```

### Example 4: Creating and Running a New Program

```bash
# 1. Create a new assembly program
echo "LOC 100
LDR 0,0,200
STR 0,0,201
HLT
DATA 42" > myprogram.src

# 2. Assemble it
java -cp out Assembler.Assembler

# 3. Run in simulator
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
# Load output.lst and run
```

## Examples

### Example 1: Simple Load and Store

```
; Load value from memory into register
LDR 0,0,0,100    ; R0 = Memory[100]

; Store register value to memory
STR 0,0,0,200    ; Memory[200] = R0

; Halt program
HLT
```

### Example 2: Indexed Addressing

```
; Load index register
LDX 1,0,50       ; X1 = 50

; Use indexed addressing
LDR 2,0,1,10     ; R2 = Memory[10 + X1] = Memory[60]
STR 2,0,1,20     ; Memory[20 + X1] = R2 = Memory[70]
```

### Example 3: Conditional Jump

```
; Load value into register
LDR 0,0,0,100    ; R0 = Memory[100]

; Jump if zero
JZ 0,0,0,200     ; If R0 == 0, jump to address 200

; Continue execution
LDR 1,0,0,101    ; R1 = Memory[101]
HLT              ; Halt
```

### Example 4: Arithmetic Operations

```
; Load two values
LDR 0,0,0,100    ; R0 = Memory[100]
LDR 1,0,0,101    ; R1 = Memory[101]

; Add memory to register
AMR 0,0,0,102    ; R0 = R0 + Memory[102]

; Store result
STR 0,0,0,103    ; Memory[103] = R0
```

## Advanced Usage

### Debugging Techniques

1. **Step through program**: Use Step button to trace execution
2. **Check registers**: Monitor register values after each instruction
3. **Check memory**: Use Load/Store to examine memory contents
4. **Set breakpoints**: Manually set PC to specific addresses

### Performance Monitoring

1. **Instruction count**: Count steps to measure program length
2. **Memory usage**: Monitor which memory locations are accessed
3. **Register usage**: Track which registers are modified

### Program Development

1. **Write assembly**: Create `.src` files with assembly code
2. **Assemble**: Use the assembler to create `.lst` files
3. **Test**: Load and run in simulator
4. **Debug**: Use step-by-step execution to find errors
5. **Optimize**: Modify code for better performance

## Tips and Best Practices

1. **Always use IPL**: Load programs before running
2. **Check PC value**: Ensure Program Counter is set correctly
3. **Use Step mode**: For debugging and learning
4. **Monitor registers**: Watch how values change
5. **Test thoroughly**: Verify all instructions work correctly
6. **Document programs**: Add comments to assembly code
7. **Use meaningful labels**: Make programs easier to understand
