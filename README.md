# Basic Machine Simulator

A collaborative Java implementation of a 16-bit computer simulator with assembler, CPU core, memory system, and graphical user interface.

## Prerequisites

Before running the Basic Machine Simulator, ensure you have the following installed:

### Required Software

- **Java 11 or higher** (OpenJDK recommended)
- **JavaFX runtime** (included with OpenJDK 11+)
- **Linux/Windows/macOS** operating system

### Check Java Installation

```bash
java -version
javac -version
```

If Java is not installed:

- **Ubuntu/Debian**: `sudo apt install openjdk-11-jdk openjfx`
- **Windows**: Download from [Oracle](https://www.oracle.com/java/) or [OpenJDK](https://openjdk.java.net/)
- **macOS**: `brew install openjdk@11` or download from Oracle

## Quick Start

### 1. Download/Clone the Project

```bash
# If using Git
git clone <repository-url>
cd Assembler

# Or download and extract the ZIP file
# Navigate to the extracted folder
```

### 2. Compile the Project

```bash
# Compile main source code
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# Compile test suite (optional)
javac -cp out -d out tests/*.java
```

### 3. Run the Simulator

#### Option A: GUI Simulator (Recommended)

```bash
# Linux
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# Windows (adjust JavaFX path as needed)
java --module-path "C:\Program Files\Java\javafx-11\lib" --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# macOS
java --module-path /opt/homebrew/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### Option B: CLI Simulator

```bash
java -cp out BasicMachine.CPU_Module.MainSimulator
```

#### Option C: Assembler Only

```bash
java -cp out Assembler.Assembler
```

### 4. Run Tests

```bash
# Run all tests
./run_tests.sh

# Or run specific tests
java -cp out TestMemorySystem
java -cp out TestLoader
java -cp out FinalTestSuite
```

## Detailed Instructions

### Running the GUI Simulator

1. **Start the GUI**:

   ```bash
   java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
   ```

2. **Load a Program**:

   - Click "Browse" button
   - Navigate to and select `output.lst` (or any `.lst` file)
   - Click "IPL" (Initial Program Load)

3. **Execute the Program**:

   - Use "Step" to execute one instruction at a time
   - Use "Run" to execute continuously
   - Use "Halt" to stop execution

4. **Monitor Registers**:
   - Watch register values change in real-time
   - Use blue buttons to copy register values
   - Monitor memory operations

### Running the CLI Simulator

1. **Start the CLI**:

   ```bash
   java -cp out BasicMachine.CPU_Module.MainSimulator
   ```

2. **Observe Output**:
   - The simulator will automatically load `load.ld`
   - Watch register states printed in octal format
   - Program will execute until halt

### Using the Assembler

1. **Prepare Assembly Code**:

   - Create a `.src` file with assembly code
   - Example: `source.src`

2. **Run the Assembler**:

   ```bash
   java -cp out Assembler.Assembler
   ```

3. **Check Output**:
   - `output.lst` - Listing file with addresses and machine code
   - `load.ld` - Load file for the simulator

## Project Structure

```
Assembler/
├── src/                        # Source code
│   ├── Assembler/              # Two-pass assembler
│   └── BasicMachine/           # Simulator components
│       ├── CPU_Module/         # CPU core implementation
│       ├── Memory_Module/      # Memory system
│       └── simulator/          # GUI interface
├── tests/                      # Test suite
│   ├── TestMemorySystem.java   # Memory system unit tests
│   ├── TestLoader.java         # Program loader unit tests
│   ├── FinalTestSuite.java     # Comprehensive test suite
│   ├── test1_basic_operations.src  # Test assembly programs
│   └── test_*.lst              # Test data files
├── documentation/              # Documentation
│   ├── architecture/           # System architecture
│   └── user/                   # User guides
├── out/                        # Compiled classes
├── load.ld                     # Generated load file
├── output.lst                  # Generated listing file
└── source.src                  # Assembly source
```

## Test Suite

### Unit Tests

- **Memory System**: Memory operations, MAR, MBR, error handling
- **Program Loader**: File loading and parsing
- **CPU Core**: Register operations, instruction execution

### Integration Tests

- **GUI Integration**: User interface with backend systems
- **Memory-CPU Integration**: Data flow between components

### Test Programs

- **Basic Operations**: LOAD, STORE, HALT instructions
- **Program Counter**: PC increment validation
- **Memory Cycles**: Complete store-load-halt cycles

### Running Tests

```bash
# Run all tests
./run_tests.sh

# Run specific tests
java -cp out TestMemorySystem
java -cp out TestLoader
java -cp out FinalTestSuite
```

## Features

### Memory System

- 2048 words of 16-bit memory
- Single-port memory (one operation per cycle)
- Invalid address error handling
- Memory reset functionality

### CPU Core

- 4 General Purpose Registers (R0-R3)
- 3 Index Registers (X1-X3)
- Special registers (PC, MAR, MBR, IR, CC, MFR)
- 16-bit instruction format
- Multiple addressing modes

### Assembler

- Two-pass assembly process
- Symbol table generation
- Machine code generation
- Listing file output

### GUI Interface

- JavaFX graphical interface
- Real-time register display
- Step-by-step execution
- Memory operations
- Program loading

## Instruction Set

| Opcode | Mnemonic | Description                   |
| ------ | -------- | ----------------------------- |
| 0      | HLT      | Halt program execution        |
| 1      | LDR      | Load register from memory     |
| 2      | STR      | Store register to memory      |
| 3      | LDA      | Load accumulator              |
| 4      | AMR      | Add memory to register        |
| 5      | SMR      | Subtract memory from register |
| 6      | AIR      | Add immediate to register     |
| 7      | SIR      | Subtract immediate            |
| 8      | JZ       | Jump if zero                  |
| 9      | JNE      | Jump if not equal             |
| 10     | JCC      | Jump if carry clear           |
| 11     | JMA      | Jump unconditional            |

## Usage Examples

### Basic Program

```assembly
LOC     100
START:  DATA    42          ; Store value 42
        LDR     0,0,100     ; R0 = Memory[100]
        STR     0,0,102     ; Memory[102] = R0
        HLT                 ; Stop execution
```

### Indexed Addressing

```assembly
LDX     1,0,50       ; X1 = 50
LDR     2,0,1,10     ; R2 = Memory[10 + X1] = Memory[60]
STR     2,0,1,20     ; Memory[20 + X1] = R2 = Memory[70]
```

### Complete Example Workflow

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

## Troubleshooting

### Common Issues

#### 1. JavaFX Not Found

**Error**: `Error: JavaFX runtime components are missing`
**Solution**:

```bash
# Install JavaFX
sudo apt install openjfx

# Or use system JavaFX
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### 2. Class Not Found

**Error**: `Could not find or load main class`
**Solution**:

```bash
# Ensure compilation was successful
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# Check if classes exist
ls -la out/
```

#### 3. GUI Won't Start

**Error**: GUI window doesn't appear
**Solution**:

- Check if JavaFX is properly installed
- Try running from terminal to see error messages
- Ensure display is available (for remote connections)

#### 4. Program Won't Load

**Error**: IPL fails or no program loads
**Solution**:

- Check if `output.lst` exists
- Verify file format (octal addresses and machine code)
- Try loading a different `.lst` file

### Platform-Specific Instructions

#### Linux (Ubuntu/Debian)

```bash
# Install dependencies
sudo apt update
sudo apt install openjdk-11-jdk openjfx

# Compile and run
javac -d out src/**/*.java
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### Windows

```bash
# Install Java 11+ and JavaFX
# Download from Oracle or use OpenJDK

# Compile
javac -d out src\**\*.java

# Run GUI
java --module-path "C:\Program Files\Java\javafx-11\lib" --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### macOS

```bash
# Install using Homebrew
brew install openjdk@11

# Compile and run
javac -d out src/**/*.java
java --module-path /opt/homebrew/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

### Error Messages

- `Memory read/write error`: Invalid address (outside 0-2047)
- `Unknown opcode X`: Illegal instruction encountered
- `IPL failed`: File loading error

## Documentation

- **[Architecture Overview](documentation/architecture/ARCHITECTURE.md)** - System design and components
- **[Instruction Format](documentation/architecture/INSTRUCTION_FORMAT.md)** - Complete instruction reference
- **[User Guide](documentation/user/USER_GUIDE.md)** - Detailed usage instructions
