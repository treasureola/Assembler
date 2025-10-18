# Basic Machine Simulator - Architecture Overview

## System Architecture

The Basic Machine Simulator implements a simplified 16-bit computer architecture with the following components:

### Memory System

- **Capacity**: 2048 words (0-2047 addresses)
- **Word Size**: 16 bits per word
- **Access**: Single-port memory (one operation per clock cycle)
- **Initialization**: All memory locations reset to zero on startup

### CPU Registers

#### General Purpose Registers (GPR)

- **R0, R1, R2, R3**: 16-bit general purpose registers
- **Purpose**: Store operands and intermediate results
- **Access**: Direct addressing in instructions

#### Index Registers (IXR)

- **X1, X2, X3**: 16-bit index registers
- **Purpose**: Provide indexed addressing capability
- **Usage**: Add to effective address for memory operations

#### Special Registers

- **PC (Program Counter)**: 12-bit register holding current instruction address
- **MAR (Memory Address Register)**: 12-bit register holding memory address for operations
- **MBR (Memory Buffer Register)**: 16-bit register holding data for memory transfers
- **IR (Instruction Register)**: 16-bit register holding current instruction
- **CC (Condition Code)**: 4-bit register storing processor status flags
- **MFR (Machine Fault Register)**: 16-bit register for error reporting

### Instruction Format

All instructions are 16 bits wide with the following format:

```
[15:10] [9:8] [7:6] [5] [4:0]
 Opcode   R    IX   I   Address
  6-bit  2-bit 2-bit 1-bit 5-bit
```

#### Field Descriptions:

- **Opcode (6 bits)**: Instruction type (LDR, STR, HLT, etc.)
- **R (2 bits)**: General purpose register (0-3)
- **IX (2 bits)**: Index register (0-3, where 0 = no indexing)
- **I (1 bit)**: Indirect addressing flag (0 = direct, 1 = indirect)
- **Address (5 bits)**: Memory address or immediate value

### Addressing Modes

1. **Direct Addressing**: Effective Address = Address field
2. **Indexed Addressing**: Effective Address = Address + IXR[IX]
3. **Indirect Addressing**: Effective Address = Memory[Address + IXR[IX]]

### Instruction Set

#### Memory Operations

- **LDR r,ix,i,addr**: Load register from memory
- **STR r,ix,i,addr**: Store register to memory
- **LDA r,ix,i,addr**: Load accumulator

#### Control Operations

- **HLT**: Halt program execution
- **JZ r,ix,i,addr**: Jump if zero
- **JNE r,ix,i,addr**: Jump if not equal
- **JMA r,ix,i,addr**: Jump unconditional

#### Arithmetic Operations

- **AMR r,ix,i,addr**: Add memory to register
- **SMR r,ix,i,addr**: Subtract memory from register
- **AIR r,ix,i,addr**: Add immediate to register

### Clock Cycle

The simulator operates on a single-phase clock:

1. **Fetch**: MAR ← PC, MBR ← Memory[MAR], IR ← MBR, PC ← PC + 1
2. **Decode**: Extract opcode, register, and addressing fields
3. **Execute**: Perform the specified operation
4. **Update**: Update condition codes and status registers

### Memory Interface

The memory system provides:

- **Read Operation**: MAR → Memory → MBR
- **Write Operation**: MAR + MBR → Memory
- **Error Handling**: Invalid addresses return 0 and log errors
- **Reset**: All locations cleared to zero

### Error Handling

The system handles:

- **Invalid Memory Addresses**: Addresses outside 0-2047 range
- **Illegal Opcodes**: Unknown instruction codes
- **Memory Errors**: Read/write failures
- **Register Overflow**: 16-bit value truncation

## Integration Points

### Memory System

- Provides Memory, MAR, MBR classes
- Handles memory operations and error checking
- Implements single-cycle memory access

### CPU Core

- Implements instruction execution
- Manages register operations
- Handles control flow

### GUI Interface

- Provides JavaFX interface
- Displays register states
- Enables step-by-step execution

### Testing & Documentation

- Validates all functionality
- Provides test programs
- Documents system behavior
