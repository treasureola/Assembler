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
            case InstructionSet.TRAP -> {
                int trapVector = ir & 0xFF;
                execTRAP(trapVector);
                printExec("TRAP");
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

    private void execTRAP(int trapVector) {
        printToPrinter(String.format("[TRAP] Trap vector %d activated", trapVector));
        
        switch (trapVector) {
            case 0: // TRAP 0: Read file (6 sentences into memory at address 0300)
                // For simulation: Load sample paragraph into memory
                loadSampleParagraph();
                printToPrinter("[TRAP 0] File read: 6 sentences loaded into memory at 0300");
                break;
                
            case 1: // TRAP 1: Get user input (word stored at address 0400)
                // For simulation: Get input from user via console
                getUserInput();
                printToPrinter("[TRAP 1] User input received and stored at 0400");
                break;
                
            default:
                printToPrinter(String.format("[TRAP] Unknown trap vector: %d", trapVector));
                break;
        }
    }
    
    private void loadSampleParagraph() {
        // Sample 6 sentences for testing
        String[] sentences = {
            "The quick brown fox jumps over the lazy dog.",
            "Programming is fun and challenging at the same time.",
            "Computer science teaches us how to solve problems.",
            "Assembly language is low level but very powerful.",
            "Cache memory improves system performance significantly.",
            "Operating systems manage hardware and software resources."
        };
        
        int baseAddr = 0x300; // 0300 octal = 192 decimal
        int addr = baseAddr;
        
        for (String sentence : sentences) {
            for (char c : sentence.toCharArray()) {
                memory.writeWordDirect(addr, (int) c);
                addr++;
            }
            // Add null terminator after each sentence
            memory.writeWordDirect(addr, 0);
            addr++;
        }
        
        printToPrinter(String.format("[TRAP 0] Loaded %d sentences starting at address %04o", 
            sentences.length, baseAddr));
    }
    
    private void getUserInput() {
        // For simulation: Use a default test word
        // In a real implementation, this would prompt the user
        String testWord = "the"; // Default test word
        int baseAddr = 0x400; // 0400 octal = 256 decimal
        
        for (int i = 0; i < testWord.length(); i++) {
            memory.writeWordDirect(baseAddr + i, (int) testWord.charAt(i));
        }
        // Null terminator
        memory.writeWordDirect(baseAddr + testWord.length(), 0);
        
        printToPrinter(String.format("[TRAP 1] Input word '%s' stored at address %04o", 
            testWord, baseAddr));
    }

    // Helper method for execution logging
    private void printExec(String instr) {
        narrate(String.format("Executed %s at PC=%04o", instr, regs.getPC()));
    }

    // Calculate effective address
    private int calculateEA(int ix, int i, int addr) {
        int ea = addr;
        if (ix > 0) ea += regs.getIXRValue(ix - 1);
        if (i == 1) ea = memory.readForCPU(ea) & 0x7FF;
        return ea & 0x7FF;
    }

    // Instruction execution methods (stubs for now)
    private void execLDR(int r, int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        int value = memory.readForCPU(ea) & 0xFFFF;
        regs.setGPRValue(r, value);
    }

    private void execSTR(int r, int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        memory.writeWord(ea, regs.getGPRValue(r) & 0xFFFF);
    }

    private void execLDA(int r, int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        regs.setGPRValue(0, ea); // LDA always loads into R0
    }

    private void execLDX(int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        int value = memory.readForCPU(ea) & 0xFFFF;
        if (ix > 0) regs.setIXRValue(ix - 1, value);
    }

    private void execSTX(int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        if (ix > 0) memory.writeWord(ea, regs.getIXRValue(ix - 1) & 0xFFFF);
    }

    private void execAMR(int r, int ix, int i, int a11) {
        int ea = calculateEA(ix, i, a11);
        int value = memory.readForCPU(ea) & 0xFFFF;
        regs.setGPRValue(r, (regs.getGPRValue(r) + value) & 0xFFFF);
    }

    private void execSMR(int r, int ix, int i, int a11) {
        int ea = calculateEA(ix, i, a11);
        int value = memory.readForCPU(ea) & 0xFFFF;
        regs.setGPRValue(r, (regs.getGPRValue(r) - value) & 0xFFFF);
    }

    private void execAIR(int r, int a5) {
        regs.setGPRValue(r, (regs.getGPRValue(r) + a5) & 0xFFFF);
    }

    private void execSIR(int r, int a5) {
        regs.setGPRValue(r, (regs.getGPRValue(r) - a5) & 0xFFFF);
    }

    private void execSRC(int r, int i, int a5) {
        // Shift/rotate - simplified implementation
        int count = a5 & 0x1F;
        int value = regs.getGPRValue(r);
        if (i == 0) regs.setGPRValue(r, (value << count) & 0xFFFF);
        else regs.setGPRValue(r, (value >>> count) & 0xFFFF);
    }

    private void execRRC(int r, int a5) {
        // Rotate right - simplified
        int count = a5 & 0x1F;
        int value = regs.getGPRValue(r);
        regs.setGPRValue(r, ((value >>> count) | (value << (16 - count))) & 0xFFFF);
    }

    private void execRFS(int a5) {
        // Return from subroutine - simplified
        regs.setPC(a5 & 0x7FF);
    }

    private void execMLT(int r, int ix) {
        // Multiply - simplified
        int result = (regs.getGPRValue(r) * regs.getGPRValue(ix)) & 0xFFFF;
        regs.setGPRValue(r, result);
    }

    private void execDVD(int r, int ix) {
        // Divide - simplified
        int divisor = regs.getGPRValue(ix);
        if (divisor != 0) {
            regs.setGPRValue(r, (regs.getGPRValue(r) / divisor) & 0xFFFF);
        }
    }

    private void execTRR(int r, int ix) {
        // Test register - simplified
        int result = (regs.getGPRValue(r) == regs.getGPRValue(ix)) ? 1 : 0;
        regs.setCC(result);
    }

    private void execAND(int r, int ix) {
        regs.setGPRValue(r, (regs.getGPRValue(r) & regs.getGPRValue(ix)) & 0xFFFF);
    }

    private void execORR(int r, int ix) {
        regs.setGPRValue(r, (regs.getGPRValue(r) | regs.getGPRValue(ix)) & 0xFFFF);
    }

    private void execNOT(int r) {
        regs.setGPRValue(r, (~regs.getGPRValue(r)) & 0xFFFF);
    }

    private void execJZ(int r, int ix, int i, int a5) {
        if (regs.getGPRValue(r) == 0) {
            int ea = calculateEA(ix, i, a5);
            regs.setPC(ea);
        }
    }

    private void execJNE(int r, int ix, int i, int a5) {
        if (regs.getGPRValue(r) != 0) {
            int ea = calculateEA(ix, i, a5);
            regs.setPC(ea);
        }
    }

    private void execJCC(int r, int ix, int i, int a5) {
        if (regs.getCC() == 1) {
            int ea = calculateEA(ix, i, a5);
            regs.setPC(ea);
        }
    }

    private void execJMA(int ix, int i, int a5) {
        int ea = calculateEA(ix, i, a5);
        regs.setPC(ea);
    }

    private void execJSR(int ix, int i, int a5) {
        // Jump to subroutine - simplified
        int ea = calculateEA(ix, i, a5);
        regs.setGPRValue(3, regs.getPC()); // Save return address in R3
        regs.setPC(ea);
    }

    private void execSOB(int r, int ix, int i, int a5) {
        int value = regs.getGPRValue(r);
        regs.setGPRValue(r, (value - 1) & 0xFFFF);
        if (regs.getGPRValue(r) != 0) {
            int ea = calculateEA(ix, i, a5);
            regs.setPC(ea);
        }
    }

    private void execJGE(int r, int ix, int i, int a5) {
        int value = regs.getGPRValue(r);
        if ((value & 0x8000) == 0) { // Positive (MSB = 0)
            int ea = calculateEA(ix, i, a5);
            regs.setPC(ea);
        }
    }

    private void execIN(int r, int dev) {
        // Input - simplified: read from memory address based on device
        int addr = 0x200 + dev; // Device buffer area
        int value = memory.readForCPU(addr) & 0xFFFF;
        regs.setGPRValue(r, value);
        printToPrinter(String.format("[IN] R%d ← dev=%d value=%04o", r, dev, value));
    }

    private void execOUT(int r, int dev) {
        // Output - simplified: write to printer
        int value = regs.getGPRValue(r) & 0xFF; // Only low byte
        printToPrinter(String.format("%c", (char)value));
    }

    private void execHLT() {
        halted = true;
        printToPrinter("[HALT] Program finished.");
    }
}
