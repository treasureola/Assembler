Design Note : Java Assembler

1\. Introduction

This project implements a simple assembler in Java. Its purpose is to
translate a custom assembly language into 16-bit machine code. The
assembler supports labels, basic instructions, and directives such as
LOC and DATA. The final outputs are a listing file for debugging and a
load file for execution.

2\. System Overview

The assembler works in two passes:

Pass 1 builds a symbol table by scanning labels and handling directives.

Pass 2 encodes instructions into 16-bit binary strings using the opcode
table and symbol table.

Outputs:

output.lst: address, binary code, and original source line,

load.ld: address--code pairs for execution.

3\. Design Goals and Constraints

Goals:

Correct translation of assembly code into binary.

Support for a small but representative instruction set.

Human-readable and machine-readable outputs.

Constraints:

Instructions are limited to 16 bits with fixed fields.

Address field is only 5 bits (0--31).

4\. Implementation Details

> Input lines are normalized, comments removed, and tokens split by
> space or comma.

Labels are stored in uppercase in the symbol table.

Instructions are encoded with fields:

opcode (6 bits), R (2 bits), IX (2 bits), I (1 bit), addr (5 bits).

Special cases:

LDX ignores the R field.

Directives:

LOC changes the current location counter.

DATA reserves one word.

Final machine codes are written to memory and exported to files.

5\. Testing Plan

Objective

> Verify that the assembler correctly handles labels, directives (LOC,
> DATA), instruction encoding (LDX, LDR, LDA, JZ, HLT), and address
> relocation.

Pass Criteria

All directives update the location counter correctly.

Symbol table contains all labels with correct addresses.

Instruction encodings match the defined opcode table.

Output files reflect correct addresses, codes, and source lines.

6\. Conclusion

The assembler achieves its goal of translating a small assembly language
into 16-bit code. It is simple and demonstrates the two-pass approach
clearly. Future
