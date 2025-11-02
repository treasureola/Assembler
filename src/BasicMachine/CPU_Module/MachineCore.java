package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.simulator.ui.SimulatorController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/* ==============================================================
 * MachineCore.java
 * --------------------------------------------------------------
 * Implements the CPU fetch–decode–execute cycle.
 * This version extends Part-1 with full ISA support that matches
 * the Part-0 Assembler’s opcode table.
 *
 * WORD FORMAT: [ opcode(6) | R(2) | IX(2) | I(1) | addr/imm5(5) ]
 *
 * Register conventions used here:
 *  - R0..R3  : general registers
 *  - X1..X3  : index registers (stored in regs.IXR[0..2])
 *  - R3      : link register for JSR/RFS
 *  - CC bits : [3:Overflow, 2:Underflow, 1:Zero, 0:Equal]
 *
 * Notes on encoding assumptions:
 *  1) Memory-operand ops use EA derived from (IX, I, addr5).
 *  2) Immediate ops use imm5 = addr5 & 0x1F (zero-extended).
 *  3) Register–register ops use R as left operand, and IX (0..3)
 *     selects the right register Ry = R[IX]. Adjust the helper
 *     rightRegIndex(r, ix) if your spec encodes Ry differently.
 *  4) SRC/RRC: LR bit = addr5>>4, COUNT = addr5 & 0xF; I bit chooses
 *     logical(0) vs arithmetic(1) for shifts.
 * ==============================================================
 */
public class MachineCore {

    private final Memory memory;
    private final Registers regs;

    private boolean halted = false;
    private Runnable onCycleListener;

    // Allows the UI printer (TextArea) to receive CPU messages
    private java.util.function.Consumer<String> printerOutput;

    public void setPrinterOutput(java.util.function.Consumer<String> printerOutput) {
        this.printerOutput = printerOutput;
    }

    // Utility printer
    private void printToPrinter(String msg) {
        if (printerOutput != null) printerOutput.accept(msg + "\n");
        else System.out.println(msg);
    }


    public void setOnCycleListener(Runnable r) { this.onCycleListener = r; }

    // Trap vector base (can be changed to course-specified value)
//    private static final int TRAP_BASE = 0400; // octal 0400 = decimal 256

    public MachineCore(Memory memory, Registers regs) {
        this.memory = memory;
        this.regs = regs;
    }

    /* --------------------------------------------------------------
     * Public control
     * -------------------------------------------------------------- */
    public void run() {
        halted = false;
        while (!halted) {
            runOneCycle();
        }
    }


    public void runOneCycle() {
        if (halted) return;
        fetch();
        decodeAndExecute();

        // 🔍 Debug: show when cache is accessed
        System.out.printf("[DEBUG] PC=%04o | IR=%06o | Hits=%d | Misses=%d%n",
                regs.getPC(), regs.getIR(), memory.getCache().getHits(), memory.getCache().getMisses());

        if (onCycleListener != null) onCycleListener.run();  // UI refresh callback
    }


    public void stop() { halted = true; }
    public boolean isHalted() { return halted; }

    /* --------------------------------------------------------------
     * Fetch
     * -------------------------------------------------------------- */
    private void fetch() {
        int pc = regs.getPC();
        regs.setMAR(pc);
        int word = memory.readWord(pc) & 0xFFFF;
        regs.setMBR(word);
        regs.setIR(word);
        regs.setPC((pc + 1) & 0x7FF);
    }

    private void decodeAndExecute() {
        int ir = regs.getIR() & 0xFFFF;

        int op  = (ir >> 10) & 0x3F;
        int r   = (ir >> 8)  & 0x03;
        int ix  = (ir >> 6)  & 0x03;
        int i   = (ir >> 5)  & 0x01;
        int a5  =  ir        & 0x1F;

        switch (op) {
            // Load / Store / Addr
            case InstructionSet.LDR -> { execLDR(r, ix, i, a5); printExec("LDR"); }
            case InstructionSet.STR -> { execSTR(r, ix, i, a5); printExec("STR"); }
            case InstructionSet.LDA -> { execLDA(r, ix, i, a5); printExec("LDA"); }
            case InstructionSet.LDX -> { execLDX(ix, i, a5);   printExec("LDX"); }
            case InstructionSet.STX -> { execSTX(ix, i, a5);   printExec("STX"); }

            // Arithmetic / Immediate
            case InstructionSet.AMR -> { execAMR(r, ix, i, a5); printExec("AMR"); }
            case InstructionSet.SMR -> { execSMR(r, ix, i, a5); printExec("SMR"); }
            case InstructionSet.AIR -> { execAIR(r, a5);         printExec("AIR"); }
            case InstructionSet.SIR -> { execSIR(r, a5);         printExec("SIR"); }

            // Branch / Control
            case InstructionSet.JZ  -> { execJZ(r, ix, i, a5);  printExec("JZ"); }
            case InstructionSet.JNE -> { execJNE(r, ix, i, a5); printExec("JNE"); }
            case InstructionSet.JCC -> { execJCC(r, ix, i, a5); printExec("JCC"); }
            case InstructionSet.JMA -> { execJMA(ix, i, a5);    printExec("JMA"); }
            case InstructionSet.JSR -> { execJSR(ix, i, a5);    printExec("JSR"); }
            case InstructionSet.RFS -> { execRFS(a5);           printExec("RFS"); }
            case InstructionSet.SOB -> { execSOB(r, ix, i, a5); printExec("SOB"); }
            case InstructionSet.JGE -> { execJGE(r, ix, i, a5); printExec("JGE"); }

            // Logical / Register–Register
            case InstructionSet.MLT -> { execMLT(r, ix); printExec("MLT"); }
            case InstructionSet.DVD -> { execDVD(r, ix); printExec("DVD"); }
            case InstructionSet.TRR -> { execTRR(r, ix); printExec("TRR"); }
            case InstructionSet.AND -> { execAND(r, ix); printExec("AND"); }
            case InstructionSet.ORR -> { execORR(r, ix); printExec("ORR"); }
            case InstructionSet.NOT -> { execNOT(r);     printExec("NOT"); }

            // Shifts / Rotates
            case InstructionSet.SRC -> { execSRC(r, i, a5); printExec("SRC"); }
            case InstructionSet.RRC -> { execRRC(r, a5);   printExec("RRC"); }

            // I/O  (read device from the low 8 bits of IR)
            case InstructionSet.IN  -> {
                int dev = ir & 0xFF;        // low 8 bits
                execIN(r, dev);
                printExec("IN");
            }
            case InstructionSet.OUT -> {
                int dev = ir & 0xFF;        // low 8 bits
                execOUT(r, dev);
                printExec("OUT");
            }

            // Halt
            case InstructionSet.HLT -> { execHLT(); printExec("HLT"); }

            default -> {
                System.out.printf("[WARN] Unknown opcode %03o at PC=%04o%n", op, regs.getPC());
                halted = true;
            }
        }
    }


    /* ---------------- EFFECTIVE ADDRESS (10-bit) ---------------- */
    // 11-bit addresses (0..2047): page size = 32 (low 5 bits)
    private int calcEA(int ix, int i, int addr5) {
        int pcAfterFetch = regs.getPC() & 0x7FF;        // 11-bit
        int page         = pcAfterFetch & 0x7E0;        // keep PC’s upper 6 bits (bits 10..5)
        int ea           = (page | (addr5 & 0x1F)) & 0x7FF;

        if (ix > 0) ea = (ea + (regs.getIXRValue(ix - 1) & 0x7FF)) & 0x7FF;
        if (i == 1) ea = memory.readWord(ea) & 0x7FF;

        return ea;
    }


    /* ---------------- UTILITIES ---------------- */
    private int ryIndex(int ix) { return Math.max(0, Math.min(3, ix)); }

    private void ccZero(int v) {
        int cc = regs.getCC();
        if ((v & 0xFFFF) == 0) cc |= 0b0010; else cc &= ~0b0010;
        regs.setCC(cc);
    }

    private void ccEqual(boolean eq) {
        int cc = regs.getCC();
        if (eq) cc |= 0b0001; else cc &= ~0b0001;
        regs.setCC(cc);
    }

    /* ---------------- IMPLEMENTATIONS ---------------- */

    // ---- Load / Store / Addr ----
    private void execLDR(int r, int ix, int i, int a5) {
        regs.setGPRValue(r, memory.readWord(calcEA(ix, i, a5)) & 0xFFFF);
    }
    private void execSTR(int r, int ix, int i, int a5) {
        memory.writeWord(calcEA(ix, i, a5), regs.getGPRValue(r));
    }
    private void execLDA(int r, int ix, int i, int a5) {
        regs.setGPRValue(r, calcEA(ix, i, a5));
    }
    private void execLDX(int ix, int i, int a5) {
        if (ix == 0) return;
        regs.setIXRValue(ix - 1, memory.readWord(calcEA(ix, i, a5)) & 0xFFFF);
    }
    private void execSTX(int ix, int i, int a5) {
        if (ix == 0) return;
        memory.writeWord(calcEA(ix, i, a5), regs.getIXRValue(ix - 1));
    }

    // ---- Arithmetic / Immediate ----
    private void execAMR(int r, int ix, int i, int a5) {
        int val = regs.getGPRValue(r) + (memory.readWord(calcEA(ix, i, a5)) & 0xFFFF);
        regs.setGPRValue(r, val & 0xFFFF); ccZero(val);
    }
    private void execSMR(int r, int ix, int i, int a5) {
        int val = regs.getGPRValue(r) - (memory.readWord(calcEA(ix, i, a5)) & 0xFFFF);
        regs.setGPRValue(r, val & 0xFFFF); ccZero(val);
    }
    private void execAIR(int r, int a5) {
        int val = (regs.getGPRValue(r) + (a5 & 0x1F)) & 0xFFFF;
        regs.setGPRValue(r, val); ccZero(val);
    }
    private void execSIR(int r, int a5) {
        int val = (regs.getGPRValue(r) - (a5 & 0x1F)) & 0xFFFF;
        regs.setGPRValue(r, val); ccZero(val);
    }

    // ---- Logical / Reg–Reg ----
    private void execAND(int r, int ix) {
        int ry = ryIndex(ix);
        int val = regs.getGPRValue(r) & regs.getGPRValue(ry);
        regs.setGPRValue(r, val); ccZero(val);
    }
    private void execORR(int r, int ix) {
        int ry = ryIndex(ix);
        int val = regs.getGPRValue(r) | regs.getGPRValue(ry);
        regs.setGPRValue(r, val); ccZero(val);
    }
    private void execNOT(int r) {
        int val = (~regs.getGPRValue(r)) & 0xFFFF;
        regs.setGPRValue(r, val); ccZero(val);
    }
    private void execTRR(int r, int ix) {
        int ry = ryIndex(ix);
        ccEqual((regs.getGPRValue(r) & 0xFFFF) == (regs.getGPRValue(ry) & 0xFFFF));
    }

    // ---- Multiply / Divide ----
    private void execMLT(int r, int ix) {
        int ry = ryIndex(ix);
        int a = regs.getGPRValue(r & ~1) & 0xFFFF;
        int b = regs.getGPRValue(ry) & 0xFFFF;
        int prod = a * b;
        regs.setGPRValue(r & ~1,  prod & 0xFFFF);
        regs.setGPRValue((r & ~1) + 1, (prod >> 16) & 0xFFFF);
        ccZero(prod);
    }
    private void execDVD(int r, int ix) {
        int ry = ryIndex(ix);
        int divisor = regs.getGPRValue(ry) & 0xFFFF;
        if (divisor == 0) { regs.setCC(regs.getCC() | 0b1000); return; }
        int dividend = regs.getGPRValue(r & ~1) & 0xFFFF;
        regs.setGPRValue(r & ~1, dividend / divisor);
        regs.setGPRValue((r & ~1) + 1, dividend % divisor);
    }

    // ---- Branch / Control ----
    private void execJZ(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0xFFFF) == 0)
            regs.setPC(calcEA(ix, i, a5));
    }
    private void execJNE(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0xFFFF) != 0)
            regs.setPC(calcEA(ix, i, a5));
    }
    private void execJCC(int r, int ix, int i, int a5) {
        int bit = Math.max(0, Math.min(3, r));
        if ((regs.getCC() & (1 << bit)) != 0)
            regs.setPC(calcEA(ix, i, a5));
    }
    private void execJMA(int ix, int i, int a5) {
        regs.setPC(calcEA(ix, i, a5));
    }
    private void execJSR(int ix, int i, int a5) {
        regs.setGPRValue(3, regs.getPC());
        regs.setPC(calcEA(ix, i, a5));
    }
    private void execRFS(int a5) {
        regs.setGPRValue(0, a5 & 0x1F);
        regs.setPC(regs.getGPRValue(3));
    }
    private void execSOB(int r, int ix, int i, int a5) {
        int val = (regs.getGPRValue(r) - 1) & 0xFFFF;
        regs.setGPRValue(r, val);
        if (val != 0) regs.setPC(calcEA(ix, i, a5));
    }
    private void execJGE(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0x8000) == 0)
            regs.setPC(calcEA(ix, i, a5));
    }

    // ---- Shifts / Rotates ----
    private void execSRC(int r, int i, int a5) {
        int lr = (a5 >> 4) & 1;
        int count = a5 & 0xF;
        int v = regs.getGPRValue(r) & 0xFFFF;
        if (count == 0) return;
        if (lr == 1) v = (v << count) & 0xFFFF;
        else if (i == 1) { int s = v & 0x8000; for (int k=0;k<count;k++) v=(v>>>1)|s; }
        else v >>>= count;
        regs.setGPRValue(r, v & 0xFFFF);
    }
    private void execRRC(int r, int a5) {
        int lr = (a5 >> 4) & 1;
        int count = a5 & 0xF;
        int v = regs.getGPRValue(r) & 0xFFFF;
        if (count == 0) return;
        if (lr == 1) v=((v<<count)|(v>>>(16-count)))&0xFFFF;
        else v=((v>>>count)|(v<<(16-count)))&0xFFFF;
        regs.setGPRValue(r, v);
    }

    // ---- I/O ----
    private static String guiInputBuffer = null;
    public static void setGuiInput(String val) { guiInputBuffer = val; }

    private void execIN(int r, int devId) {
        int val = 0;

        // Notify user via Printer
        printToPrinter(String.format("[INPUT] Awaiting input for device"));

        // Wait for GUI input if available
        if (SimulatorController.activeInstance != null) {
            String userInput = null;

            // 🚦 Wait until the user actually types something in Console Input
            while (userInput == null) {
                userInput = SimulatorController.activeInstance.pollConsoleInput();
                try {
                    Thread.sleep(100); // check every 0.1s
                } catch (InterruptedException ignored) {}
            }

            // Parse the value safely
            try {
                val = Integer.parseInt(userInput.trim());
            } catch (NumberFormatException e) {
                val = 0;
            }

            printToPrinter(String.format("[INPUT] Received value %d for R%d.", val, r));
            System.out.printf("[DEBUG IN-GUI] R%d ← %d (%06o)%n", r, val, val);

        } else {
            // fallback for terminal use
            System.out.print("[INPUT dev=" + devId + "] > ");
            java.util.Scanner sc = new java.util.Scanner(System.in);
            val = sc.nextInt();
            System.out.printf("[DEBUG IN-TERMINAL] R%d ← %d (%06o)%n", r, val, val);
        }

        // Store the result in the register
        regs.setGPRValue(r, val);
    }







    private void execOUT(int r, int dev) {
        System.out.printf("[OUT] dev=%d ← R%d=%06o%n", dev, r, regs.getGPRValue(r)&0xFFFF);
    }

    // ---- HALT ----
    private void execHLT() {
        halted = true;
        System.out.println("[HLT] CPU halted.");
    }

    /* --------------------------------------------------------------
     * Pretty printer for executed instructions
     * -------------------------------------------------------------- */
    private void printExec(String name) {
        String msg = String.format("[EXEC] Instruction %s executed.", name);
        printToPrinter(msg);
    }



}
