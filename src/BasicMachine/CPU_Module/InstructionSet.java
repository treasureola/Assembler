package BasicMachine.CPU_Module;

/* ==============================================================
 * InstructionSet.java
 * --------------------------------------------------------------
 * Defines all instruction opcodes recognized by the Basic Machine
 * CPU. Each opcode corresponds exactly to the encoding scheme
 * used by the assembler and matches the approved .ld output.
 *
 * Instruction Encoding (16 bits total):
 * ------------------------------------
 * | Bits | Field | Description                 |
 * |------|-------|-----------------------------|
 * |15-10 | opcode| Operation code (6 bits)     |
 * | 9-8  | R     | General-purpose register ID |
 * | 7-6  | IX    | Index register (0..3)       |
 * |  5   | I     | 1=indirect, 0=direct       |
 * | 4-0  | addr  | 5-bit address / immediate  |
 *
 * NOTE: For register–register instructions (TRR/MLT/DVD/etc.),
 *       we use R as the left operand and IX (0..3) as the right
 *       operand’s index (mapped to R0..R3). See MachineCore notes.
 * ==============================================================
 */
public final class InstructionSet {

    private InstructionSet() {}

    // Core (Part 1)
    public static final int HLT = 00;
    public static final int LDR = 01;
    public static final int STR = 02;
    public static final int LDA = 03;
    public static final int LDX = 041;
    public static final int STX = 042;

    // Branch / Control
    public static final int JZ  = 010;
    public static final int JNE = 011;
    public static final int JCC = 012;
    public static final int JMA = 013;
    public static final int JSR = 014;
    public static final int RFS = 015;
    public static final int SOB = 016;
    public static final int JGE = 017;

    // Arithmetic / Immediate
    public static final int AMR = 04;
    public static final int SMR = 05;
    public static final int AIR = 06;
    public static final int SIR = 07;

    // Multiply / Divide
    public static final int MLT = 070;
    public static final int DVD = 071;

    // Logical / Test / Reg-Reg
    public static final int TRR = 072;
    public static final int AND = 073;
    public static final int ORR = 074;
    public static final int NOT = 075;

    // Shifts / Rotates
    public static final int SRC = 031;
    public static final int RRC = 032;

    // I/O
    public static final int IN  = 061;
    public static final int OUT = 062;
    public static final int CHK = 063;  // R <- device ready (1) or not (0)

    // Trap
    public static final int TRAP = 024; // Trap to OS handler
}
