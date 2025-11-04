package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.simulator.ui.SimulatorController;

import java.util.Scanner;

/**
 * MachineCore.java
 * --------------------------------------------------------------
 * Implements the CPU fetch–decode–execute cycle for the Basic Machine.
 * Extends Part 1 with full instruction set architecture support
 * consistent with the assembler’s opcode table.
 */
public class MachineCore {

    private final Memory memory;
    private final Registers regs;
    private boolean halted = false;
    private Runnable onCycleListener;
    private java.util.function.Consumer<String> printerOutput;
    private ExecutionListener listener;

    public void setExecutionListener(ExecutionListener l) { this.listener = l; }
    private void narrate(String s) { if (listener != null) listener.onNarrative(s); }
    public void setPrinterOutput(java.util.function.Consumer<String> printerOutput) { this.printerOutput = printerOutput; }
    private void printToPrinter(String msg) {
        if (printerOutput != null) printerOutput.accept(msg + "\n");
        else System.out.println(msg);
    }
    public void setOnCycleListener(Runnable r) { this.onCycleListener = r; }

    public MachineCore(Memory memory, Registers regs) {
        this.memory = memory;
        this.regs = regs;
    }

    public void run() {
        halted = false;
        while (!halted) runOneCycle();
    }

    public void runOneCycle() {
        if (halted) return;
        fetch();
        decodeAndExecute();

        System.out.printf("[DEBUG] PC=%04o | IR=%06o | Hits=%d | Misses=%d%n",
                regs.getPC(), regs.getIR(), memory.getCache().getHits(), memory.getCache().getMisses());

        if (onCycleListener != null) onCycleListener.run();
    }

    public void stop() { halted = true; }
    public boolean isHalted() { return halted; }

    private void fetch() {
        int pc = regs.getPC();
        regs.setMAR(pc);
        int word = memory.readForCPU(pc) & 0xFFFF;
        regs.setMBR(word);
        regs.setIR(word);
        regs.setPC((pc + 1) & 0x7FF);
    }

    private void decodeAndExecute() {
        int ir  = regs.getIR() & 0xFFFF;
        int op  = (ir >> 10) & 0x3F;
        int r   = (ir >> 8)  & 0x03;
        int ix  = (ir >> 6)  & 0x03;
        int i   = (ir >> 5)  & 0x01;

        int a11 = ir & 0x7FF;
        int a5  = ir & 0x1F;

        System.out.printf("[TRACE] opcode=%d IR=%06o%n", op, ir);
        switch (op) {
            case InstructionSet.LDR -> { execLDR(r, ix, i, a5); printExec("LDR");}
            case InstructionSet.STR -> { execSTR(r, ix, i, a5); printExec("STR"); }
            case InstructionSet.LDA -> { execLDA(r, ix, i, a5); printExec("LDA"); }
            case InstructionSet.LDX -> { execLDX(ix, i, a5);    printExec("LDX");  }
            case InstructionSet.STX -> { execSTX(ix, i, a5);    printExec("STX");  }
            case InstructionSet.AMR -> { execAMR(r, ix, i, a11); printExec("AMR"); }
            case InstructionSet.SMR -> { execSMR(r, ix, i, a11); printExec("SMR"); }
            case InstructionSet.AIR -> { execAIR(r, a5);         printExec("AIR"); }
            case InstructionSet.SIR -> { execSIR(r, a5);         printExec("SIR"); }
            case InstructionSet.SRC -> { execSRC(r, i, a5);      printExec("SRC"); }
            case InstructionSet.RRC -> { execRRC(r, a5);         printExec("RRC"); }
            case InstructionSet.RFS -> { execRFS(a5);            printExec("RFS"); }
            case InstructionSet.MLT -> { execMLT(r, ix);         printExec("MLT"); }
            case InstructionSet.DVD -> { execDVD(r, ix);         printExec("DVD"); }
            case InstructionSet.TRR -> { execTRR(r, ix);         printExec("TRR"); }
            case InstructionSet.AND -> { execAND(r, ix);         printExec("AND"); }
            case InstructionSet.ORR -> { execORR(r, ix);         printExec("ORR"); }
            case InstructionSet.NOT -> { execNOT(r);             printExec("NOT"); }
            case InstructionSet.JZ  -> { execJZ (r, ix, i, a5);  printExec("JZ");  }
            case InstructionSet.JNE -> { execJNE(r, ix, i, a5);  printExec("JNE"); }
            case InstructionSet.JCC -> { execJCC(r, ix, i, a5);  printExec("JCC"); }
            case InstructionSet.JMA -> { execJMA(ix, i, a5);     printExec("JMA"); }
            case InstructionSet.JSR -> { execJSR(ix, i, a5);     printExec("JSR"); }
            case InstructionSet.SOB -> { execSOB(r, ix, i, a5);  printExec("SOB"); }
            case InstructionSet.JGE -> { execJGE(r, ix, i, a5);  printExec("JGE"); }
            case InstructionSet.IN  -> {
                int dev = ir & 0xFF;
                execIN(r, dev);
                printExec("IN");
            }
            case InstructionSet.OUT -> {
                int dev = ir & 0xFF;
                execOUT(r, dev);
                printExec("OUT");
            }
            case InstructionSet.HLT -> { execHLT(); printExec("HLT"); }
            default -> System.out.printf("[WARN] Unknown opcode %03o at PC=%04o%n", op, regs.getPC());
        }
    }

    public int calcEA(int ix, int i, int addr) {
        int ea = (addr & 0x7FF) < 040 ? addr & 0x1F : addr & 0x7FF;
        if (ix > 0) ea = (ea + regs.getIXRValue(ix - 1)) & 0x7FF;
        if (i == 1) ea = memory.readForCPU(ea) & 0x7FF;
        return ea;
    }

    private int ryIndex(int ix) { return Math.max(0, Math.min(3, ix)); }
    private void ccZero(int v) { int cc = regs.getCC(); if ((v & 0xFFFF) == 0) cc |= 0b0010; else cc &= ~0b0010; regs.setCC(cc); }
    private void ccEqual(boolean eq) { int cc = regs.getCC(); if (eq) cc |= 0b0001; else cc &= ~0b0001; regs.setCC(cc); }

    // ---- Load / Store / Address ----
    private void execLDR(int r, int ix, int i, int a5) {
        int ea = calcEA(ix, i, a5);
        int val = memory.readForCPU(ea) & 0xFFFF;
        if (r == 0 && ix == 1 && i == 0 && a5 == 0) {
            int x1 = regs.getIXRValue(0) & 0x7FF;
            System.out.printf("[DBG:LDR R0,1,0] X1=%04o EA=%04o M[EA]=%06o%n", x1, ea, val);
        }
        regs.setGPRValue(r, val);
    }

    private void execSTR(int r, int ix, int i, int a5) {
        int ea = calcEA(ix, i, a5);
        memory.writeFromCPU(ea, regs.getGPRValue(r));
        System.out.printf("[TRACE] STR R%d -> mem[%06o] = %06o%n", r, ea, regs.getGPRValue(r));
    }

    private void execLDA(int r, int ix, int i, int a5) {
        regs.setGPRValue(r, calcEA(ix, i, a5));
    }

    private void execLDX(int ix, int i, int a5) {
        if (ix == 0) return;
        int ea = calcEA(ix, i, a5);     // Use the same EA logic as LDR
        int val = memory.readForCPU(ea) & 0xFFFF;
        regs.setIXRValue(ix - 1, val);
    }


    private void execSTX(int ix, int i, int a5) {
        if (ix == 0) return;
        int ea = calcEA(ix, i, a5);     // ✅ Use full addressing logic
        int val = regs.getIXRValue(ix - 1) & 0xFFFF;
        memory.writeFromCPU(ea, val);
        System.out.printf("[STX] M[%04o] <- X%d = %06o%n", ea, ix, val);
    }


    private void execAMR(int r, int ix, int i, int a5) {
        int val = regs.getGPRValue(r) + (memory.readForCPU(calcEA(ix, i, a5)) & 0xFFFF);
        regs.setGPRValue(r, val & 0xFFFF); ccZero(val);
    }
    private void execSMR(int r, int ix, int i, int a5) {
        int val = regs.getGPRValue(r) - (memory.readForCPU(calcEA(ix, i, a5)) & 0xFFFF);
        regs.setGPRValue(r, val & 0xFFFF); ccZero(val);
    }
    private void execAIR(int r, int a5) {
        int val = (regs.getGPRValue(r) + (a5 & 0x1F)) & 0x7FF;
        regs.setGPRValue(r, val); ccZero(val);
    }
    private void execSIR(int r, int a5) {
        int before = regs.getGPRValue(r);
        int val = (before - (a5 & 0x1F)) & 0x7FF;
        regs.setGPRValue(r, val);
        System.out.printf("[DEBUG] SIR R%d: %06o -> %06o (imm=%o)%n", r, before, val, a5);
    }

    // ---- Logical / Register Operations ----
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

    private void execJZ(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0xFFFF) == 0) regs.setPC(calcEA(ix, i, a5));
    }
    private void execJNE(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0xFFFF) != 0) regs.setPC(calcEA(ix, i, a5));
    }
    private void execJCC(int r, int ix, int i, int a5) {
        int bit = Math.max(0, Math.min(3, r));
        if ((regs.getCC() & (1 << bit)) != 0) regs.setPC(calcEA(ix, i, a5));
    }
    private void execJMA(int ix, int i, int a5) { regs.setPC(calcEA(ix, i, a5)); }
    private void execJSR(int ix, int i, int a5) {
        regs.setGPRValue(3, regs.getPC());
        regs.setPC(calcEA(ix, i, a5));
    }
    private void execRFS(int a5) {
        regs.setGPRValue(0, a5 & 0x1F);
        regs.setPC(regs.getGPRValue(3));
    }
    private void execSOB(int r, int ix, int i, int a5) {
        int before = regs.getGPRValue(r);
        int val = before - 1;
        if ((val & 0xFFFF) == 0)
            System.out.println("[STOP] R" + r + " reached zero → exiting loop");
        regs.setGPRValue(r, val & 0xFFFF);
        int ea = calcEA(ix, i, a5);
        System.out.printf("[DEBUG] SOB R%d: %06o -> %06o | EA=%06o%n", r, before, val, ea);
        if ((regs.getGPRValue(r) & 0xFFFF) != 0) regs.setPC(ea);
    }
    private void execJGE(int r, int ix, int i, int a5) {
        if ((regs.getGPRValue(r) & 0x8000) == 0) regs.setPC(calcEA(ix, i, a5));
    }

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

    private static String guiInputBuffer = null;
    public static void setGuiInput(String val) { guiInputBuffer = val; }

    private void execIN(int r, int devId) {
        int val = 0;
        printToPrinter(String.format("[IN] Waiting for input from device %d...\n", devId));
        try {
            if (SimulatorController.activeInstance != null) {
                var controller = SimulatorController.activeInstance;
                controller.appendPrinter(String.format("[IN] Device %d awaiting input...\n", devId));
                String userInput = null;
                while (userInput == null || userInput.isBlank()) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    userInput = controller.pollConsoleInput();
                }
                try { val = Integer.parseInt(userInput.trim()); }
                catch (NumberFormatException e) { val = 0; }
            }
        } catch (Throwable ignored) { }
        if (val == 0 && (SimulatorController.activeInstance == null)) {
            System.out.print("[IN dev=" + devId + "] > ");
            Scanner sc = new Scanner(System.in);
            try { val = sc.nextInt(); } catch (Exception e) { val = 0; }
        }
        regs.setGPRValue(r, val);
        printToPrinter(String.format("[IN] R%d ← dev=%d value=%06o", r, devId, val & 0xFFFF));
    }

    private boolean printedReadingMessage = false;

    private void execOUT(int r, int dev) {
        int v = regs.getGPRValue(r) & 0xFFFF;
        if (!printedReadingMessage) {
            narrate("Reading 20 values... this might take a while.");
            printedReadingMessage = true;
        }
        printToPrinter(String.format("%d ", (short)v));
    }

    private void execHLT() {
        halted = true;
        printedReadingMessage = false;
        System.out.println("[HLT] CPU halted.");
    }

    private void printExec(String name) { }
}
