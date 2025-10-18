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

- Java 11 or higher
- JavaFX runtime (included with OpenJDK 11+)
- Linux/Windows/macOS operating system

### Installation

1. Clone or download the simulator project
2. Ensure Java and JavaFX are installed
3. Compile the project using the provided build scripts

### Quick Start

1. **Start the GUI**: Run `SimulatorApp.java`
2. **Load a program**: Click Browse and select a `.lst` file
3. **Initialize**: Click IPL (Initial Program Load)
4. **Run**: Use Step or Run buttons to execute

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

### Common Issues

#### 1. GUI Won't Start

- **Check JavaFX**: Ensure JavaFX is installed
- **Check Java version**: Use Java 11 or higher
- **Check classpath**: Ensure all classes are compiled

#### 2. Program Won't Load

- **Check file format**: Ensure `.lst` file is correct
- **Check file path**: Verify file exists and is readable
- **Check file content**: Look for proper octal format

#### 3. Program Won't Run

- **Check PC value**: Ensure Program Counter is set correctly
- **Check memory**: Verify program is loaded in memory
- **Check instructions**: Look for illegal opcodes

#### 4. Registers Not Updating

- **Check execution**: Ensure program is actually running
- **Check instruction**: Verify instruction is valid
- **Check addressing**: Ensure memory addresses are correct

### Error Messages

#### "IPL failed: [message]"

- File not found or invalid format
- Check file path and content

#### "Unknown opcode X, halting"

- Invalid instruction encountered
- Check program for errors

#### "Memory read/write error: invalid address X"

- Address outside valid range (0-2047)
- Check addressing calculations

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
