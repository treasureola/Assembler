package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;

/* ==============================================================
 * MachineCore.java
 * --------------------------------------------------------------
 * Implements the CPU fetch–decode–execute cycle for the Basic
 * Machine Simulator (Part 1). This class represents the core
 * logic of the simulated CPU, handling instruction sequencing,
 * address calculation, and execution.
 *
 * Supported Instructions:
 *   - LDR : Load Register from Memory
 *   - STR : Store Register to Memory
 *   - LDX : Load Index Register
 *   - LDA : Load Register with Address
 *   - JZ  : Jump if Zero
 *   - HLT : Halt execution
 *
 * Instruction Encoding (16 bits total):
 *   [15..10] = opcode (6 bits)
 *   [ 9.. 8] = R       (2 bits)
 *   [ 7.. 6] = IX      (2 bits)
 *   [   5  ] = I       (1 bit)  (0=direct, 1=indirect)
 *   [ 4.. 0] = address (5 bits)
 *
 * Example (from listing):
 *   102207 → opcode=33 (LDX), r=0, ix=2, i=0, addr=7
 *   003412 → opcode=1  (LDR), r=3, ix=0, i=0, addr=10
 *   020100 → opcode=8  (JZ),  r=0, ix=1, i=0, addr=0
 *   000000 → opcode=0  (HLT)
 * ==============================================================
 */
public class MachineCore {

    private final Memory memory;   // Reference to system memory
    private final Registers regs;  // CPU register file
    private boolean halted = false;

    // Optional UI listener — used by the simulator GUI (SimulatorController)
    private Runnable onCycleListener;
    public void setOnCycleListener(Runnable r) { this.onCycleListener = r; }

    /* --------------------------------------------------------------
     * Constructor
     * --------------------------------------------------------------
     * Initializes the CPU core with a reference to Memory and
     * Registers, allowing the core to directly manipulate them.
     */
    public MachineCore(Memory memory, Registers regs) {
        this.memory = memory;
        this.regs = regs;
    }

    /* --------------------------------------------------------------
     * run()
     * --------------------------------------------------------------
     * Executes the program continuously until a HALT (HLT) instruction
     * is reached or an external stop() is triggered by the simulator.
     */
    public void run() {
        halted = false;
        while (!halted) {
            runOneCycle();
        }
    }

    /* --------------------------------------------------------------
     * runOneCycle()
     * --------------------------------------------------------------
     * Executes exactly one complete instruction cycle:
     *   1. Fetch   — Retrieve the instruction from memory (via PC)
     *   2. Decode  — Parse opcode and operands
     *   3. Execute — Perform the required operation
     *
     * Used by the STEP button in the simulator GUI.
     */
    public void runOneCycle() {
        if (halted) return;
        fetch();
        decodeAndExecute();
        if (onCycleListener != null) onCycleListener.run();  // UI refresh callback
    }

    /* --------------------------------------------------------------
     * stop()
     * --------------------------------------------------------------
     * Manually halts CPU execution (used by HALT button).
     */
    public void stop() { halted = true; }

    public boolean isHalted() { return halted; }

    /* ==============================================================
     * FETCH PHASE
     * --------------------------------------------------------------
     * - Reads the instruction from memory using the Program Counter.
     * - Loads it into IR (Instruction Register).
     * - Increments PC for the next cycle.
     * ==============================================================
     */
    private void fetch() {
        int pc = regs.getPC();
        regs.setMAR(pc);                    // MAR ← PC
        int word = memory.readWord(pc);     // MBR ← M[PC]
        regs.setMBR(word);
        regs.setIR(word);                   // IR ← instruction
        regs.setPC((pc + 1) & 0x7FF);       // Increment PC (11-bit wraparound)
    }

    /* ==============================================================
     * DECODE + EXECUTE PHASE
     * --------------------------------------------------------------
     * - Extracts bit-fields from IR.
     * - Determines which instruction to execute.
     * - Delegates to appropriate helper method.
     * ==============================================================
     */
    private void decodeAndExecute() {
        int instr = regs.getIR() & 0xFFFF;

        // Extract bit-fields
        int opcode  = (instr >> 10) & 0x3F;  // 6-bit opcode
        int r       = (instr >> 8)  & 0x03;  // 2-bit register index
        int ix      = (instr >> 6)  & 0x03;  // 2-bit index register
        int i       = (instr >> 5)  & 0x01;  // 1-bit indirect flag
        int address =  instr        & 0x1F;  // 5-bit address field

        System.out.printf("DEBUG: IR=%06o opcode=%d r=%d ix=%d i=%d addr=%d%n",
                instr, opcode, r, ix, i, address);

        // Execute instruction
        switch (opcode) {
            case InstructionSet.LDR -> executeLDR(r, ix, i, address);
            case InstructionSet.STR -> executeSTR(r, ix, i, address);
            case InstructionSet.LDX -> executeLDX(ix, i, address);
            case InstructionSet.LDA -> executeLDA(r, ix, i, address);
            case InstructionSet.JZ  -> executeJZ(r, ix, i, address);
            case InstructionSet.HLT -> executeHLT();
            default -> {
                System.out.printf("[WARN] Unknown opcode %d (IR=%06o) at PC=%04o – halting.%n",
                        opcode, instr, regs.getPC());
                halted = true;
            }
        }
    }

    /* ==============================================================
     * EFFECTIVE ADDRESS CALCULATION
     * --------------------------------------------------------------
     * Computes the Effective Address (EA) based on:
     *   - IX (index register field)
     *   - I  (indirect bit)
     *   - addr (5-bit address field)
     *
     * EA = (I == 1) ? M[base + addr] : (base + addr)
     * base = IXR[ix-1] if ix > 0, else 0
     * ==============================================================
     */
    private int calcEA(int ix, int i, int addr5) {
        int base = addr5 & 0x1F;  // Limit to 5 bits

        // Indexed addressing (add IXR if applicable)
        if (ix > 0) {
            base = (base + regs.getIXRValue(ix - 1)) & 0x7FF;  // IXR[0]=X1, IXR[1]=X2, etc.
        }

        int ea = base & 0x7FF;

        // Indirect addressing (second level memory access)
        if (i == 1) {
            ea = memory.readWord(ea) & 0x7FF;
        }

        return ea & 0x7FF;
    }

    /* ==============================================================
     * INSTRUCTION IMPLEMENTATIONS
     * --------------------------------------------------------------
     * Each method executes a single machine instruction. All methods
     * must be consistent with the assembler encoding and instruction
     * specification.
     * ==============================================================
     */

    /** LDR r,ix,i,addr — Load Register from Memory */
    private void executeLDR(int r, int ix, int i, int addr) {
        int ea  = calcEA(ix, i, addr);
        int val = memory.readWord(ea);
        regs.setGPRValue(r, val);
        System.out.printf("[LDR] R%d ← M[%04o] = %06o%n", r, ea, val & 0xFFFF);
    }

    /** STR r,ix,i,addr — Store Register into Memory */
    private void executeSTR(int r, int ix, int i, int addr) {
        int ea  = calcEA(ix, i, addr);
        int val = regs.getGPRValue(r);
        memory.writeWord(ea, val);
        System.out.printf("[STR] M[%04o] ← R%d = %06o%n", ea, r, val & 0xFFFF);
    }

    /** LDX ix,i,addr — Load Index Register from Memory */
    private void executeLDX(int ix, int i, int addr) {
        if (ix == 0) { // LDX requires X1..X3 (ix = 1–3)
            System.out.println("[LDX] ix=0 is invalid for LDX; treating as no-op.");
            return;
        }
        int ea  = calcEA(ix, i, addr);
        int val = memory.readWord(ea);
        regs.setIXRValue(ix - 1, val);
        System.out.printf("[LDX] X%d ← M[%04o] = %06o%n", ix, ea, val & 0xFFFF);
    }

    /** LDA r,ix,i,addr — Load Register with Effective Address */
    private void executeLDA(int r, int ix, int i, int addr) {
        int ea = calcEA(ix, i, addr);
        regs.setGPRValue(r, ea);
        System.out.printf("[LDA] R%d ← EA = %06o%n", r, ea & 0xFFFF);
    }

    /** JZ r,ix,i,addr — Jump if Register r == 0 */
    private void executeJZ(int r, int ix, int i, int addr) {
        int ea = calcEA(ix, i, addr);
        if ((regs.getGPRValue(r) & 0xFFFF) == 0) {
            regs.setPC(ea);
            System.out.printf("[JZ] R%d==0 → PC ← %04o%n", r, ea);
        } else {
            System.out.printf("[JZ] R%d!=0 → no jump%n", r);
        }
    }

    /** HLT — Stop program execution */
    private void executeHLT() {
        halted = true;
        System.out.println("[HLT] CPU halted.");
    }
}
