# Basic Machine Simulator - Setup and Run Instructions

## 📋 Prerequisites

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

## 🚀 Quick Start

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
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### Option B: CLI Simulator

```bash
java -cp out BasicMachine.CPU_Module.MainSimulator
```

#### Option C: Assembler Only

```bash
java -cp out Assembler.Assembler
```

## 🎯 Detailed Instructions

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

## 🧪 Running Tests

### Run All Tests

```bash
# Use the test runner script
./run_tests.sh

# Or run manually
java -cp out FinalTestSuite
```

### Run Specific Tests

```bash
# Memory system tests
java -cp out TestMemorySystem

# Program loader tests
java -cp out TestLoader
```

## 📁 Project Structure

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

## 🔧 Troubleshooting

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
javac -d out src/main/**/*.java

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
javac -d out src/main/**/*.java
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### Windows

```bash
# Install Java 11+ and JavaFX
# Download from Oracle or use OpenJDK

# Compile
javac -d out src\main\**\*.java

# Run GUI
java --module-path "C:\Program Files\Java\javafx-11\lib" --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### macOS

```bash
# Install using Homebrew
brew install openjdk@11

# Compile and run
javac -d out src/main/**/*.java
java --module-path /opt/homebrew/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

## 📖 Example Usage

### 1. Create a Simple Program

Create `myprogram.src`:

```assembly
LOC     100
START:  DATA    42
        LDR     0,0,100
        STR     0,0,102
        HLT
```

### 2. Assemble the Program

```bash
# Copy your program to source.src
cp myprogram.src source.src

# Run assembler
java -cp out Assembler.Assembler
```

### 3. Run in Simulator

```bash
# Start GUI
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp

# Load output.lst and run
```

## 🎯 Features Overview

### Memory System

- 2048 words of 16-bit memory
- Single-port memory (one operation per cycle)
- Invalid address error handling

### CPU Core

- 4 General Purpose Registers (R0-R3)
- 3 Index Registers (X1-X3)
- Special registers (PC, MAR, MBR, IR, CC, MFR)

### Instruction Set

- LOAD/STORE operations
- Arithmetic operations
- Jump and branch instructions
- Halt instruction

### GUI Features

- Real-time register display
- Step-by-step execution
- Memory operations
- Program loading

## 📚 Additional Resources

- **Architecture Documentation**: `documentation/architecture/ARCHITECTURE.md`
- **Instruction Reference**: `documentation/architecture/INSTRUCTION_FORMAT.md`
- **User Guide**: `documentation/user/USER_GUIDE.md`
- **Project Structure**: `PROJECT_STRUCTURE.md`

## 🆘 Getting Help

If you encounter issues:

1. **Check Prerequisites**: Ensure Java 11+ and JavaFX are installed
2. **Verify Compilation**: Make sure all classes compiled successfully
3. **Check File Paths**: Ensure you're in the correct directory
4. **Review Documentation**: Check the documentation/ folder for detailed guides
5. **Run Tests**: Use `./run_tests.sh` to verify functionality

## 📝 Notes

- The simulator is designed for educational purposes
- All memory addresses are in octal format
- Instructions are 16-bit with specific format
- The GUI provides the best user experience
- CLI mode is useful for automated testing

---

**Happy Simulating!** 🚀

For questions or issues, refer to the documentation in the `docs/` folder or check the test suite for examples.
