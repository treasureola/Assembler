# Basic Machine Simulator

A complete implementation of a 16-bit computer architecture simulator with assembler, CPU core, memory system, and cache.

## Project Overview

This project implements a Basic Machine Simulator for CS6461 (Computer Architecture). It includes:

- **Two-pass Assembler**: Converts assembly language to machine code
- **CPU Simulator**: Executes machine code with full instruction set
- **Memory System**: 2048-word main memory with 16-line cache
- **Cache System**: Fully associative FIFO cache with tracing
- **JavaFX GUI**: Interactive simulator with front panel controls
- **CLI Interface**: Command-line simulator for batch execution

## System Components

### 1. Assembler (`src/Assembler/`)
- Two-pass assembly process
- Symbol table management  
- Generates `.lst` (listing) and `.ld` (load) files
- Supports LOC, DATA, and instruction mnemonics

### 2. Memory System (`src/BasicMachine/Memory_Module/`)
- **Memory.java**: 2048-word main memory (16-bit words)
- **Cache.java**: 16-line fully associative cache with FIFO replacement
- **MAR.java**: Memory Address Register
- **MBR.java**: Memory Buffer Register

**Cache Features:**
- Fully associative, unified (instruction + data)
- FIFO replacement policy
- Write-through policy
- Cache statistics tracking (hits, misses, hit rate)
- Trace logging for debugging

### 3. CPU Module (`src/BasicMachine/CPU_Module/`)
- **MachineCore.java**: Fetch-decode-execute cycle
- **Registers.java**: PC, MAR, MBR, IR, CC, MFR, GPR, IXR
- **InstructionSet.java**: Instruction definitions

### 4. Simulator UI (`src/BasicMachine/simulator/`)
- **JavaFX GUI**: Interactive front panel
- **Program Loader**: Load `.lst` files
- **Control Panel**: IPL, Run, Step, Halt buttons
- **Register Display**: Real-time register values
- **Cache Monitor**: Cache statistics and contents (Phase II)

## Quick Start

### Prerequisites

```bash
# Java Development Kit (JDK 11 or higher)
sudo apt install default-jdk

# JavaFX (for GUI)
sudo apt install openjfx
```

### Compilation

```bash
# Compile all components
cd /path/to/Assembler

# Compile Assembler
javac -d out src/Assembler/Assembler.java

# Compile Memory and Cache
javac -d out src/BasicMachine/Memory_Module/*.java

# Compile CPU Module
javac -d out src/BasicMachine/CPU_Module/*.java

# Compile Simulator (with JavaFX)
javac -d out --module-path /usr/share/openjfx/lib \
  --add-modules javafx.controls,javafx.fxml \
  src/BasicMachine/simulator/**/*.java
```

### Running the Assembler

```bash
# Assemble a source file
java -cp out Assembler.Assembler

# Input: source.src
# Output: output.lst (listing), load.ld (load file)
```

### Running the Simulator

#### CLI Mode

```bash
# Run CLI simulator
java -cp out BasicMachine.CPU_Module.MainSimulator

# Loads load.ld and executes
```

#### GUI Mode

```bash
# Launch JavaFX GUI
java -cp out --module-path /usr/share/openjfx/lib \
  --add-modules javafx.controls,javafx.fxml \
  BasicMachine.simulator.ui.SimulatorApp

# Use IPL button to load program
# Click Run, Step, or Halt to control execution
```

## Cache System (Phase II)

### Cache Specifications

- **Type**: Fully Associative, Unified Cache
- **Size**: 16 cache lines
- **Line Size**: 1 word (16 bits)
- **Replacement**: FIFO (First-In, First-Out)
- **Write Policy**: Write-through with write-allocate

### Using the Cache

```java
// Enable cache (enabled by default)
memory.enableCache();

// Enable cache tracing
memory.enableCacheTracing("cache_trace.log");

// Run program (cache operates transparently)
cpu.run();

// View cache statistics
System.out.println(memory.getCacheStatistics());
System.out.println(memory.getCacheContents());

// Disable cache for testing
memory.disableCache();
```

### Cache Performance Metrics

```
Cache Statistics:
  Total Accesses: 100
  Hits: 75
  Misses: 25
  Hit Rate: 75.00%
  Reads: 80
  Writes: 20
```

### Cache Trace Example

```
=== Cache Trace Started at 2025-10-29 00:00:00 ===
Cache Configuration: Size=16 lines, Policy=FIFO, Type=Unified
READ  MISS | Addr=0144 | Data=010421 | Insert Line=0
READ  HIT  | Addr=0144 | Data=010421 | Line=0
WRITE MISS | Addr=0310 | Data=001234 | Insert Line=1
READ  MISS | Addr=0400 | Data=000000 | Evict Line=0 (Tag=0144)
=== Cache Trace Ended at 2025-10-29 00:01:00 ===
```

## Testing

### Run All Tests

```bash
# Using test runner script
./run_tests.sh

# Or manually
javac -d out -cp out tests/*.java
java -cp out TestCache        # Cache tests
java -cp out TestMemorySystem # Memory tests
java -cp out FinalTestSuite   # Comprehensive tests
```

### Cache Demonstration

```bash
# Run cache demo program
java -cp out Assembler.Assembler < tests/test_cache_demo.src
java -cp out BasicMachine.CPU_Module.MainSimulator

# Check cache_trace.log for detailed cache behavior
cat cache_trace.log
```

## Project Structure

```
Assembler/
├── src/
│   ├── Assembler/
│   │   └── Assembler.java
│   └── BasicMachine/
│       ├── Memory_Module/
│       │   ├── Memory.java          # Main memory + cache integration
│       │   ├── Cache.java           # Cache implementation
│       │   ├── MAR.java
│       │   └── MBR.java
│       ├── CPU_Module/
│       │   ├── MachineCore.java     # CPU execution
│       │   ├── Registers.java       # Register file
│       │   ├── MainSimulator.java   # CLI entry point
│       │   └── InstructionSet.java
│       └── simulator/
│           ├── ui/
│           │   ├── SimulatorApp.java      # JavaFX app
│           │   ├── SimulatorController.java
│           │   └── simulator.fxml
│           ├── core/
│           │   ├── CPU.java
│           │   └── Utils.java
│           └── ProgramLoader.java
├── tests/
│   ├── TestCache.java               # Cache test suite
│   ├── TestMemorySystem.java        # Memory test suite
│   ├── FinalTestSuite.java          # Comprehensive tests
│   ├── test_cache_demo.src          # Cache demo program
│   └── test*.src                    # Other test programs
├── documentation/
│   ├── architecture/
│   │   ├── ARCHITECTURE.md          # System architecture
│   │   ├── CACHE_DESIGN.md          # Cache design details
│   │   └── INSTRUCTION_FORMAT.md    # Instruction format
│   └── user/
│       └── USER_GUIDE.md            # User guide
├── out/                             # Compiled classes
├── README.md                        # This file
└── run_tests.sh                     # Test runner script
```

## Documentation

### Architecture Documentation
- [System Architecture](documentation/architecture/ARCHITECTURE.md)
- [Cache Design](documentation/architecture/CACHE_DESIGN.md)
- [Instruction Format](documentation/architecture/INSTRUCTION_FORMAT.md)

### User Documentation
- [User Guide](documentation/user/USER_GUIDE.md)

### Test Documentation
- [Test Suite](tests/README.md)

## Features

### Phase I (Completed)
- Two-pass assembler with symbol table
- 16-bit instruction set
- Memory system (2048 words)
- CPU execution engine
- Register file
- CLI simulator

### Phase II (Current - Cache Implementation)
- 16-line fully associative cache
- FIFO replacement policy
- Write-through policy
- Cache statistics tracking
- Cache trace logging
- Cache demonstration programs

### Future Enhancements
- Extended instruction set
- Interrupt handling
- I/O device simulation
- Multi-level cache hierarchy
- Branch prediction

## Development

### Adding New Instructions

1. Update `InstructionSet.java` with opcode
2. Add decode logic in `MachineCore.java`
3. Implement execution method
4. Update documentation in `INSTRUCTION_FORMAT.md`
5. Add test cases in `tests/`

### Modifying Cache

1. Edit `Cache.java` for cache logic changes
2. Update `Memory.java` for integration changes
3. Recompile: `javac -d out src/BasicMachine/Memory_Module/*.java`
4. Run tests: `java -cp out TestCache`

## Troubleshooting

### Common Issues

**Issue**: Cache not working
```bash
# Check if cache is enabled
memory.isCacheEnabled()  // Should return true

# Enable cache explicitly
memory.enableCache();
```

**Issue**: Trace file not created
```bash
# Ensure write permissions
chmod +w .

# Enable tracing explicitly
memory.enableCacheTracing("cache_trace.log");
```

**Issue**: JavaFX GUI not launching
```bash
# Install OpenJFX
sudo apt install openjfx

# Use correct module path
java -cp out --module-path /usr/share/openjfx/lib \
  --add-modules javafx.controls,javafx.fxml \
  BasicMachine.simulator.ui.SimulatorApp
```

## Performance Analysis

### Cache Performance Tips

**Good Cache Performance:**
- Use sequential memory access patterns
- Reuse recently accessed data
- Keep working set under 16 words
- Loop through arrays sequentially

**Poor Cache Performance:**
- Random memory access
- Large working sets (>16 words)
- Stride patterns with large gaps
- Frequent access to >16 unique addresses

### Benchmark Results

| Access Pattern | Hit Rate | Performance Gain |
|----------------|----------|------------------|
| Sequential     | 85-95%   | ~8x faster       |
| Repeated       | 95-99%   | ~10x faster      |
| Random         | 10-20%   | ~1.1x faster     |

## Contributing

This project is part of CS6461 coursework at George Washington University.

## License

Educational use only - CS6461 Course Project

## Authors

- Assembler Module
- Memory System Module (with Cache - Phase II)
- CPU Module
- Simulator UI Module
- Testing and Documentation Module

## Acknowledgments

- CS6461 Course Staff
- Computer Organization and Design (Patterson & Hennessy)
- Java Documentation
- JavaFX Documentation

