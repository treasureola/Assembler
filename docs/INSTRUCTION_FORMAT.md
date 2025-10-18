# Instruction Format Reference

## Overview

All instructions in the Basic Machine Simulator are 16 bits wide and follow a consistent format that allows for flexible addressing modes and register operations.

## Instruction Format

```
Bit Position: 15 14 13 12 11 10 | 9  8 | 7  6 | 5 | 4  3  2  1  0
Field:        Opcode (6 bits)   | R   | IX  | I | Address (5 bits)
Size:         6 bits            | 2   | 2   | 1 | 5 bits
```

## Field Descriptions

### Opcode (Bits 15-10)

The 6-bit opcode field specifies the operation to be performed.

| Opcode | Binary | Mnemonic | Description                   |
| ------ | ------ | -------- | ----------------------------- |
| 0      | 000000 | HLT      | Halt program execution        |
| 1      | 000001 | LDR      | Load register from memory     |
| 2      | 000010 | STR      | Store register to memory      |
| 3      | 000011 | LDA      | Load accumulator              |
| 4      | 000100 | AMR      | Add memory to register        |
| 5      | 000101 | SMR      | Subtract memory from register |
| 6      | 000110 | AIR      | Add immediate to register     |
| 7      | 000111 | SIR      | Subtract immediate            |
| 8      | 001000 | JZ       | Jump if zero                  |
| 9      | 001001 | JNE      | Jump if not equal             |
| 10     | 001010 | JCC      | Jump if carry clear           |
| 11     | 001011 | JMA      | Jump unconditional            |
| 12     | 001100 | JSR      | Jump to subroutine            |
| 13     | 001101 | RFS      | Return from subroutine        |
| 14     | 001110 | SOB      | Subtract one and branch       |
| 15     | 001111 | JGE      | Jump if greater or equal      |

### Register Field (Bits 9-8)

Specifies the general purpose register (R0-R3) for the operation.

| Value | Binary | Register |
| ----- | ------ | -------- |
| 0     | 00     | R0       |
| 1     | 01     | R1       |
| 2     | 10     | R2       |
| 3     | 11     | R3       |

### Index Register Field (Bits 7-6)

Specifies the index register (X1-X3) for indexed addressing.

| Value | Binary | Index Register | Effect            |
| ----- | ------ | -------------- | ----------------- |
| 0     | 00     | None           | No indexing       |
| 1     | 01     | X1             | Add X1 to address |
| 2     | 10     | X2             | Add X2 to address |
| 3     | 11     | X3             | Add X3 to address |

### Indirect Flag (Bit 5)

Controls whether indirect addressing is used.

| Value | Binary | Addressing Mode |
| ----- | ------ | --------------- |
| 0     | 0      | Direct          |
| 1     | 1      | Indirect        |

### Address Field (Bits 4-0)

5-bit address field providing 32 possible values (0-31).

## Addressing Modes

### 1. Direct Addressing

- **Formula**: Effective Address = Address field
- **Indirect Flag**: 0
- **Index Flag**: 0
- **Example**: `LDR 1,0,0,10` loads R1 from memory address 10

### 2. Indexed Addressing

- **Formula**: Effective Address = Address field + IXR[IX]
- **Indirect Flag**: 0
- **Index Flag**: 1-3
- **Example**: `LDR 1,0,1,10` loads R1 from memory address (10 + X1)

### 3. Indirect Addressing

- **Formula**: Effective Address = Memory[Address field + IXR[IX]]
- **Indirect Flag**: 1
- **Index Flag**: 0-3
- **Example**: `LDR 1,0,0,1,10` loads R1 from memory address stored at address 10

### 4. Indexed Indirect Addressing

- **Formula**: Effective Address = Memory[Address field + IXR[IX]]
- **Indirect Flag**: 1
- **Index Flag**: 1-3
- **Example**: `LDR 1,0,1,1,10` loads R1 from memory address stored at (10 + X1)

## Instruction Examples

### Load Instructions

```
LDR 2,0,0,15     ; R2 = Memory[15] (direct)
LDR 1,0,2,10     ; R1 = Memory[10 + X2] (indexed)
LDR 3,0,0,1,20   ; R3 = Memory[Memory[20]] (indirect)
LDR 0,0,1,1,5    ; R0 = Memory[Memory[5 + X1]] (indexed indirect)
```

### Store Instructions

```
STR 2,0,0,25     ; Memory[25] = R2 (direct)
STR 1,0,3,30     ; Memory[30 + X3] = R1 (indexed)
STR 3,0,0,1,35   ; Memory[Memory[35]] = R3 (indirect)
```

### Jump Instructions

```
JZ 0,0,0,100     ; Jump to address 100 if R0 == 0
JMA 0,0,0,200    ; Unconditional jump to address 200
JNE 1,0,0,150    ; Jump to address 150 if R1 != 0
```

### Arithmetic Instructions

```
AMR 2,0,0,40     ; R2 = R2 + Memory[40]
AIR 1,0,0,5      ; R1 = R1 + 5 (immediate)
SMR 3,0,0,50     ; R3 = R3 - Memory[50]
```

## Special Instructions

### HLT (Halt)

- **Opcode**: 000000
- **Format**: `HLT`
- **Effect**: Stops program execution
- **Binary**: `000000 00 00 0 00000`

### LDA (Load Accumulator)

- **Opcode**: 000011
- **Format**: `LDA r,ix,i,addr`
- **Effect**: R0 = Memory[effective_address]
- **Note**: Always loads into R0 regardless of register field

## Binary Encoding Examples

### Example 1: LDR 2,0,0,10

```
Opcode: LDR = 1 = 000001
R: 2 = 10
IX: 0 = 00
I: 0 = 0
Address: 10 = 01010
Result: 000001 10 00 0 01010 = 0x0A0A
```

### Example 2: STR 1,0,2,1,15

```
Opcode: STR = 2 = 000010
R: 1 = 01
IX: 2 = 10
I: 1 = 1
Address: 15 = 01111
Result: 000010 01 10 1 01111 = 0x25CF
```

### Example 3: HLT

```
Opcode: HLT = 0 = 000000
R: 0 = 00
IX: 0 = 00
I: 0 = 0
Address: 0 = 00000
Result: 000000 00 00 0 00000 = 0x0000
```

## Error Conditions

### Invalid Opcodes

- Any opcode not in the supported set (0-15)
- Results in CPU halt with error message

### Invalid Registers

- Register field values > 3 are masked to 2 bits
- Index register 0 means no indexing

### Invalid Addresses

- Address field values > 31 are masked to 5 bits
- Memory addresses outside 0-2047 range cause errors

## Implementation Notes

1. **Sign Extension**: All values are treated as unsigned
2. **Overflow Handling**: 16-bit arithmetic with wraparound
3. **Memory Alignment**: All addresses are word-aligned
4. **Endianness**: Little-endian byte ordering
5. **Clock Cycles**: Each instruction takes exactly one clock cycle
