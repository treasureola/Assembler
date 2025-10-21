package BasicMachine.CPU_Module;

/* ==============================================================
 * Registers.java
 * --------------------------------------------------------------
 * Purpose:
 *   Represents the CPU register set of the Basic Machine.
 *   Each register (general, index, or control) is modeled as
 *   a simple integer field or array element.
 *
 * Description:
 *   - GPR[4] : General Purpose Registers (R0–R3)
 *   - IXR[3] : Index Registers (X1–X3)
 *   - PC     : Program Counter (11 bits)
 *   - MAR    : Memory Address Register (11 bits)
 *   - MBR    : Memory Buffer Register (16 bits)
 *   - IR     : Instruction Register (16 bits)
 *   - MFR    : Machine Fault Register (16 bits)
 *   - CC     : Condition Code (4 bits used: Overflow, Underflow, Zero, Equal)
 *
 * Usage:
 *   - The MachineCore uses these registers during fetch–decode–execute.
 *   - The Simulator GUI (Part 1) binds directly to these fields
 *     to reflect live state changes as instructions execute.
 * ==============================================================
 */
public class Registers {

    /* --------------------------------------------------------------
     * CPU REGISTER ARRAYS
     * --------------------------------------------------------------
     * GPR and IXR are modeled as integer arrays. This structure
     * allows indexed access such as GPR[0] for R0, IXR[1] for X2, etc.
     */
    private final int[] GPR = new int[4];   // R0, R1, R2, R3
    private final int[] IXR = new int[3];   // X1, X2, X3

    /* --------------------------------------------------------------
     * CONTROL AND SPECIAL-PURPOSE REGISTERS
     * --------------------------------------------------------------
     * These control registers drive CPU operation and communication
     * with memory during each instruction cycle.
     */
    private int PC;   // Program Counter (11-bit address)
    private int MAR;  // Memory Address Register (11-bit address)
    private int MBR;  // Memory Buffer Register (16-bit word)
    private int IR;   // Instruction Register (16-bit instruction)
    private int MFR;  // Machine Fault Register (fault codes)
    private int CC;   // Condition Code (4-bit flags)

    /* ==============================================================
     * RESET METHOD
     * --------------------------------------------------------------
     * Called at startup (e.g., on IPL) to clear all registers.
     * ==============================================================
     */
    public void reset() {
        for (int i = 0; i < GPR.length; i++) GPR[i] = 0;
        for (int i = 0; i < IXR.length; i++) IXR[i] = 0;
        PC = MAR = MBR = IR = MFR = CC = 0;
    }

    /* ==============================================================
     * GENERAL-PURPOSE AND INDEX REGISTER ACCESSORS
     * --------------------------------------------------------------
     * These provide direct access to R0–R3 and X1–X3.
     * Bit masking ensures all values remain within valid width.
     * ==============================================================
     */

    /** Returns the full GPR array (for GUI or debugging). */
    public int[] getGPR() { return GPR; }

    /** Returns the full IXR array (for GUI or debugging). */
    public int[] getIXR() { return IXR; }

    /** Returns the value of register R[r]. */
    public int getGPRValue(int r) { return GPR[r]; }

    /** Sets R[r] = val (masked to 16 bits). */
    public void setGPRValue(int r, int val) { GPR[r] = val & 0xFFFF; }

    /** Returns the value of index register X[x]. (0→X1, 1→X2, 2→X3) */
    public int getIXRValue(int x) { return IXR[x]; }

    /** Sets X[x] = val (masked to 16 bits). */
    public void setIXRValue(int x, int val) { IXR[x] = val & 0xFFFF; }

    /* ==============================================================
     * CONTROL REGISTER ACCESSORS
     * --------------------------------------------------------------
     * Each register includes a setter and getter.
     * Bit masks enforce correct word width per the architecture spec.
     * ==============================================================
     */

    /** Program Counter (PC) — 11-bit value (0–2047). */
    public int getPC() { return PC; }
    public void setPC(int val) { PC = val & 0x7FF; }

    /** Memory Address Register (MAR) — 11-bit address. */
    public int getMAR() { return MAR; }
    public void setMAR(int val) { MAR = val & 0x7FF; }

    /** Memory Buffer Register (MBR) — 16-bit data word. */
    public int getMBR() { return MBR; }
    public void setMBR(int val) { MBR = val & 0xFFFF; }

    /** Instruction Register (IR) — 16-bit instruction. */
    public int getIR() { return IR; }
    public void setIR(int val) { IR = val & 0xFFFF; }

    /** Machine Fault Register (MFR) — 16-bit fault flags. */
    public int getMFR() { return MFR; }
    public void setMFR(int val) { MFR = val & 0xFFFF; }

    /** Condition Code (CC) — 4-bit status flags. */
    public int getCC() { return CC; }
    public void setCC(int val) { CC = val & 0xF; }

    /* ==============================================================
     * NOTES:
     * --------------------------------------------------------------
     * - The simulator GUI continuously polls these fields to update
     *   visual elements such as binary LED indicators and text boxes.
     * - MachineCore interacts with these during every cycle:
     *     MAR ← PC
     *     MBR ← M[MAR]
     *     IR  ← MBR
     *     PC  ← PC + 1
     * - The register model is intentionally simple, mirroring the
     *   original architecture’s direct memory interface.
     * ==============================================================
     */
}
