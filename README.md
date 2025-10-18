# Basic Machine Simulator

A collaborative Java implementation of a 16-bit computer simulator with assembler, CPU core, memory system, and graphical user interface.

## 🏗️ Project Structure

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
└── out/                        # Compiled classes
```

## 🏗️ Components

- **Memory System**: Memory, MAR, MBR implementation
- **CPU Core**: Registers, Instruction Execution
- **GUI Interface**: JavaFX Frontend
- **Testing & Documentation**: Test Suite, Documentation

## 🚀 Quick Start

### Prerequisites

- Java 11 or higher
- JavaFX runtime (included with OpenJDK 11+)

### Compilation

```bash
# Compile main source
javac -d out src/Assembler/*.java
javac -d out src/BasicMachine/**/*.java

# Compile tests
javac -cp out -d out tests/*.java
```

### Running the Simulator

#### GUI Simulator (Recommended)

```bash
java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml -cp out BasicMachine.simulator.ui.SimulatorApp
```

#### CLI Simulator

```bash
java -cp out BasicMachine.CPU_Module.MainSimulator
```

#### Assembler

```bash
java -cp out Assembler.Assembler
```

### Running Tests

```bash
# Run all tests
./run_tests.sh

# Or run specific tests
java -cp out FinalTestSuite
java -cp out TestMemorySystem
```

## 📖 Documentation

- **[Architecture Overview](documentation/architecture/ARCHITECTURE.md)** - System design and components
- **[Instruction Format](documentation/architecture/INSTRUCTION_FORMAT.md)** - Complete instruction reference
- **[User Guide](documentation/user/USER_GUIDE.md)** - How to use the simulator
- **[Project Structure](PROJECT_STRUCTURE.md)** - Detailed project organization

## 🧪 Test Suite

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

## 🎯 Features

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

## 📋 Instruction Set

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

## 🔧 Usage Examples

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

## 🐛 Troubleshooting

### Common Issues

1. **GUI won't start**: Check JavaFX installation
2. **Program won't load**: Verify .lst file format
3. **Tests fail**: Ensure all dependencies are compiled

### Error Messages

- `Memory read/write error`: Invalid address (outside 0-2047)
- `Unknown opcode X`: Illegal instruction encountered
- `IPL failed`: File loading error

## 📊 Test Results

Current test suite results:

- **Unit Tests**: 15/20 passed (75%)
- **Memory System**: ✅ All tests passed
- **Register Operations**: ✅ All tests passed
- **Program Loading**: ✅ All tests passed
- **Integration**: ✅ All tests passed

## 🤝 Contributing

### Adding New Features

1. Update appropriate component in `src/main/`
2. Add tests in `tests/unit/` or `tests/integration/`
3. Update documentation in `docs/`
4. Run integration checklist
5. Test thoroughly

### Code Style

- Use PascalCase for class names
- Use camelCase for method names
- Include comprehensive comments
- Follow Java naming conventions

## 📝 License

This project is part of a Computer Architecture course assignment.

## 📞 Support

For questions or issues:

1. Check the documentation in `documentation/`
2. Review the test suite in `tests/`
3. Consult the integration checklist
4. Check the project structure guide

---

**Testing and Documentation**  
_Comprehensive test suite and documentation for Basic Machine Simulator_
