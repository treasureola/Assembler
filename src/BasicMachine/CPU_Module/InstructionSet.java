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
    public static final int STX = 042; //added

    // Branch / Control
    public static final int JZ  = 010;
    public static final int JNE = 011;   // if R != 0  jump EA
    public static final int JCC = 012;  // if CC bit set jump EA (bit chosen by R)
    public static final int JMA = 013;  // PC <- EA
    public static final int JSR = 014;  // R3 <- return ; PC <- EA
    public static final int RFS = 015;  // R0 <- imm5 ; PC <- R3
    public static final int SOB = 016;  // R <- R-1 ; if R!=0 jump EA
    public static final int JGE = 017;  // if R >= 0 (signed) jump EA

    // Arithmetic / Immediate
    public static final int AMR = 04;   // R <- R + M[EA]
    public static final int SMR = 05;   // R <- R - M[EA]
    public static final int AIR = 06;   // R <- R + imm5
    public static final int SIR = 07;   // R <- R - imm5

    // Multiply / Divide
    public static final int MLT = 070;  // R:R+1 <- R * Ry
    public static final int DVD = 071;  // R <- R / Ry ; R+1 <- R % Ry

    // Logical / Test / Reg-Reg
    public static final int TRR = 072;  // set CC(EQUAL) if R == Ry
    public static final int AND = 073;  // R <- R & Ry
    public static final int ORR = 074;  // R <- R | Ry
    public static final int NOT = 075;  // R <- ~R

    // Shifts / Rotates
    public static final int SRC = 031;  // shift (logical/arithmetic, L/R, count)
    public static final int RRC = 032;  // rotate (L/R, count)

    // I/O
    public static final int IN  = 061;  // R <- input(dev)
    public static final int OUT = 062;  // output(dev, R)
//    public static final int CHK = 63;  // R <- device_ready(dev)

//    // Trap / Floating / Vector (declared; only TRAP executed)
//    public static final int TRAP = 24; // trap to vector
//    public static final int LDFR = 16; // (declared)
//    public static final int STFR = 41; // (declared)
//    // FADD/FSUB/VADD/VSUB/CNVRT appear in the table but are out of scope.
}
