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
 * |------|--------|-----------------------------|
 * |15-10 | opcode | Operation code (6 bits)     |
 * | 9-8  | R      | General-purpose register ID |
 * | 7-6  | IX     | Index register (0–3)        |
 * |  5   | I      | Indirect flag (1=indirect)  |
 * | 4-0  | addr   | Memory address (0–31)       |
 *
 * Example breakdown (from listing file):
 *   102207 (octal) = opcode=33 (LDX), R=0, IX=2, I=0, addr=7
 *   003412 (octal) = opcode=1  (LDR), R=3, IX=0, I=0, addr=10
 *   020100 (octal) = opcode=8  (JZ),  R=0, IX=1, I=0, addr=0
 *   000000 (octal) = opcode=0  (HLT)
 *
 * This class is referenced by:
 *   - MachineCore.java (for decode/execute switch logic)
 *   - Assembler.java   (for consistent opcode mapping)
 *   - ProgramLoader.java (for interpretation of .ld programs)
 * ==============================================================
 */
public final class InstructionSet {

    // Prevent instantiation
    private InstructionSet() {}

    /* ==============================================================
     * LOAD / STORE INSTRUCTIONS
     * --------------------------------------------------------------
     * These instructions move data between registers and memory.
     * Each corresponds to a 6-bit opcode value.
     * ==============================================================
     */

    public static final int LDR = 1;   // Load Register from Memory (opcode 000001b)
    // Format: LDR r,ix,i,addr  → R[r] ← M[EA]

    public static final int STR = 2;   // Store Register to Memory (opcode 000010b)
    // Format: STR r,ix,i,addr  → M[EA] ← R[r]

    public static final int LDA = 3;   // Load Register with Address (opcode 000011b)
    // Format: LDA r,ix,i,addr  → R[r] ← EA

    public static final int LDX = 33;  // Load Index Register (opcode 100001b)
    // Format: LDX ix,addr[,i]  → X[ix] ← M[EA]
    // Matches pattern: 102xxx (octal words in .ld file)

    /* ==============================================================
     * BRANCH / CONTROL TRANSFER INSTRUCTIONS
     * --------------------------------------------------------------
     * Used for conditional or unconditional branching.
     * ==============================================================
     */

    public static final int JZ = 8;    // Jump if Zero (opcode 001000b)
    // Format: JZ r,ix,i,addr
    // If CC = Zero → PC ← EA

    /* ==============================================================
     * MISCELLANEOUS INSTRUCTIONS
     * --------------------------------------------------------------
     * Used for stopping or halting program execution.
     * ==============================================================
     */

    public static final int HLT = 0;   // Halt execution (opcode 000000b)
    // Terminates fetch-decode-execute cycle.
    // Matches "HLT" directive in listing file.

    /* ==============================================================
     * DEBUG / UTILITY METHODS
     * --------------------------------------------------------------
     * Returns a readable mnemonic string for display or debugging.
     * Used by MachineCore.java for console output and logs.
     * ==============================================================
     */
    public static String nameOf(int opcode) {
        return switch (opcode) {
            case LDR -> "LDR";
            case STR -> "STR";
            case LDA -> "LDA";
            case LDX -> "LDX";
            case JZ  -> "JZ";
            case HLT -> "HLT";
            default  -> "OP" + opcode; // Fallback for undefined opcode
        };
    }

    /* ==============================================================
     * ADDITIONAL NOTES:
     * --------------------------------------------------------------
     * - All opcode values are decimal integers but directly
     *   correspond to binary encodings used in machine words.
     * - These constants are referenced by both the assembler and
     *   the simulator to ensure bit-level consistency.
     * - To add new instructions, assign a unique 6-bit opcode (0–63),
     *   update the assembler’s opcode table, and add execution logic
     *   in MachineCore.java under decodeAndExecute().
     * ==============================================================
     */
}
