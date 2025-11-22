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
            case InstructionSet.CHK -> {
                int dev = ir & 0xFF;
                execCHK(r, dev);
                printExec("CHK");
            }
            case InstructionSet.HLT -> { execHLT(); printExec("HLT"); }
            default -> System.out.printf("[WARN] Unknown opcode %03o at PC=%04o%n", op, regs.getPC());
        }
    }

    private void execCHK(int r, int devId) {
        int ready = 1; // For simulation, assume device is always ready
        regs.setGPRValue(r, ready);
        printToPrinter(String.format("[CHK] R%d ← dev=%d ready=%d", r, devId, ready));
    }

    // ... other methods continue unchanged
}
