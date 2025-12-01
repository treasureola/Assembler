package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;
import BasicMachine.simulator.ui.SimulatorController;

import java.io.*;

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

    // TRAP / Program 2 state
    private volatile String pendingConsoleInput = null;
    private volatile boolean waitingForTrap1Input = false;

    // Paragraph sentences loaded by TRAP 0 (from Sentences.txt)
    // TRAP 1 uses this for semantic search (sentence # / word #)
    private String[] paragraphSentences = null;

    // Base addresses used by Program 2
    private static final int PARAGRAPH_BASE_ADDR = 0x300; // 0300 (paragraph)
    private static final int WORD_BASE_ADDR      = 0x400; // 0400 (search word)

    public void setExecutionListener(ExecutionListener l) { this.listener = l; }
    private void narrate(String s) { if (listener != null) listener.onNarrative(s); }

    public void setPrinterOutput(java.util.function.Consumer<String> printerOutput) {
        this.printerOutput = printerOutput;
    }

    private void printToPrinter(String msg) {
        if (printerOutput != null) printerOutput.accept(msg + "\n");
        else System.out.println(msg);
    }

    public void provideConsoleInput(String line) {
        // called from GUI "Send" button
        if (line == null) line = "";
        this.pendingConsoleInput = line;
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

//        System.out.printf("[DEBUG] PC=%04o | IR=%06o | Hits=%d | Misses=%d%n",
//                regs.getPC(), regs.getIR(), memory.getCache().getHits(), memory.getCache().getMisses());

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

//        System.out.printf("[TRACE] opcode=%d IR=%06o%n", op, ir);
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
    }

    private void execTRAP(int trapVector) {
        switch (trapVector) {
            case 0 -> { // TRAP 0: Read paragraph from file and print it
                loadSampleParagraph();
            }
            case 1 -> { // TRAP 1: Get user word + search
                getUserInputAndSearch();
            }
            default -> printToPrinter(String.format("[TRAP] Unknown trap vector: %d\n", trapVector));
        }
    }

    // Wait for a line of input from either the GUI console (if present)
    // or from stdin (text-mode simulator fallback).
    private String waitForConsoleInput(String prompt) {
        SimulatorController ui = SimulatorController.activeInstance;
        if (ui != null) {
            ui.appendPrinter(prompt + "\n");
            return ui.pollConsoleInput();
        } else {
            // Text-mode (Phase 2) run: don't block on stdin; caller will use default
            return null;
        }
    }

    /**
     * TRAP 0:
     *  - Reads up to 6 sentences from Sentences.txt
     *  - Stores them in memory starting at PARAGRAPH_BASE_ADDR (0300)
     *  - Saves them in paragraphSentences for TRAP 1
     *  - Prints the sentences on the simulated printer
     */
    private void loadSampleParagraph() {
        try {
            File cwd = new File(System.getProperty("user.dir"));
            File f1 = new File(cwd, "Sentences.txt"); // CWD (e.g., project root or out/)
            File parent = cwd.getParentFile();
            File f2 = (parent != null) ? new File(parent, "Sentences.txt") : null;

            File f = null;
            if (f1.exists())      f = f1;
            else if (f2 != null && f2.exists()) f = f2;

            if (f == null || !f.exists()) {
                printToPrinter("[FILE ERROR] Sentences.txt not found.");
                // ensure TRAP 1 knows nothing is loaded
                paragraphSentences = null;
                return;
            }

            java.util.List<String> sentences = new java.util.ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    sentences.add(line);
                }
            }

            if (sentences.isEmpty()) {
                printToPrinter("[FILE ERROR] Sentences.txt is empty.");
                paragraphSentences = null;
                return;
            }

            // Store characters in memory starting at PARAGRAPH_BASE_ADDR,
            // null-terminated per sentence.
            int addr = PARAGRAPH_BASE_ADDR;
            for (String s : sentences) {
                for (char c : s.toCharArray()) {
                    memory.writeWordDirect(addr++, (int) c);
                }
                memory.writeWordDirect(addr++, 0); // sentence terminator
            }

            // Expose sentences to TRAP 1 for semantic search
            paragraphSentences = sentences.toArray(new String[0]);

            printToPrinter("[INFO] File Read: Reading Sentences into memory\n\n");

            // Print them out via the simulated printer (as required by Program 2)
            addr = PARAGRAPH_BASE_ADDR;
            for (int i = 1; i <= paragraphSentences.length; i++) {
                printSentenceFromMemory(i, addr);
                addr = skipToNextSentence(addr);
            }

        } catch (Exception e) {
            printToPrinter("[FILE ERROR] " + e.getMessage());
            paragraphSentences = null;
        }
    }

    private void printSentenceFromMemory(int sentenceNum, int addr) {
        StringBuilder sb = new StringBuilder();
        int val;

        while ((val = memory.readForCPU(addr++)) != 0) {
            sb.append((char)val);
        }

        printToPrinter(sb.toString() + "\n\n");
    }

    private int skipToNextSentence(int addr) {
        while (memory.readForCPU(addr) != 0) {
            addr++;
        }
        return addr + 1; // move past null terminator
    }

    /**
     * Helper used by TRAP 1:
     *  - Asks the user for the search word (via GUI console if available)
     *  - Stores it at 0400
     *  - Searches the paragraph loaded by TRAP 0 and prints the result
     */
    private void getUserInputAndSearch() {
        // Ask through GUI console if available, otherwise just use default
        String word = waitForConsoleInput(
                "[INPUT] Enter the search word in the Console Input field and press Send");

        if (word == null) {
            word = "";
        }
        word = word.trim();
        if (word.isEmpty()) {
            word = "the";   // default test word for non-GUI / empty input
        }

        // Store word at 0400 in memory (as Program 2 expects)
        int baseAddr = WORD_BASE_ADDR;
        for (int i = 0; i < word.length(); i++) {
            memory.writeWordDirect(baseAddr + i, (int) word.charAt(i));
        }
        memory.writeWordDirect(baseAddr + word.length(), 0); // null terminator

        printToPrinter(String.format("[INFO] User input received and stored at %04o\n", baseAddr));

        // Now search the paragraph and print results
        searchWordInParagraph(word);
    }

    /**
     * Searches paragraphSentences for the given word
     * and prints:
     *   "Word 'w' found in sentence S, word W"
     * or a "not found" message.
     */
    private void searchWordInParagraph(String word) {
        if (paragraphSentences == null || paragraphSentences.length == 0) {
            printToPrinter("[TRAP 1] No paragraph loaded (call TRAP 0 first).\n");
            return;
        }

        String target = word.toLowerCase();
        boolean found = false;

        for (int sIdx = 0; sIdx < paragraphSentences.length; sIdx++) {
            String sentence = paragraphSentences[sIdx];

            // Split into tokens on whitespace
            String[] tokens = sentence.split("\\s+");

            for (int wIdx = 0; wIdx < tokens.length; wIdx++) {
                // Strip punctuation for comparison
                String cleaned = tokens[wIdx].replaceAll("[^A-Za-z0-9]", "").toLowerCase();

                if (!cleaned.isEmpty() && cleaned.equals(target)) {
                    int sentenceNum = sIdx + 1;
                    int wordNum     = wIdx + 1;
                    printToPrinter(String.format(
                            "Word '%s' found in sentence %d, word %d\n",
                            word, sentenceNum, wordNum));
                    found = true;
                }
            }
        }

        if (!found) {
            printToPrinter(String.format("Word '%s' not found in paragraph.\n", word));
        }
    }

    // (Older helper retained; not used by TRAP 1 anymore but harmless)
    private void getUserInput() {
        String word = waitForConsoleInput(
                "[INPUT] Enter the search word in the Console Input field and press Send");

        if (word == null) {
            word = "";
        }
        word = word.trim();
        if (word.isEmpty()) {
            word = "the";   // default for non-GUI runs
        }

        int baseAddr = WORD_BASE_ADDR;
        for (int i = 0; i < word.length(); i++) {
            memory.writeWordDirect(baseAddr + i, (int) word.charAt(i));
        }
        memory.writeWordDirect(baseAddr + word.length(), 0);

        narrate(String.format("[TRAP 1] Input word '%s' stored at address %04o",
                word, baseAddr));
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

    // ==================== INSTRUCTION IMPLEMENTATIONS ====================

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
        int count = a5 & 0x1F;
        int value = regs.getGPRValue(r);
        if (i == 0) regs.setGPRValue(r, (value << count) & 0xFFFF);
        else regs.setGPRValue(r, (value >>> count) & 0xFFFF);
    }

    private void execRRC(int r, int a5) {
        int count = a5 & 0x1F;
        int value = regs.getGPRValue(r);
        regs.setGPRValue(r, ((value >>> count) | (value << (16 - count))) & 0xFFFF);
    }

    private void execRFS(int a5) {
        regs.setPC(a5 & 0x7FF);
    }

    private void execMLT(int r, int ix) {
        int result = (regs.getGPRValue(r) * regs.getGPRValue(ix)) & 0xFFFF;
        regs.setGPRValue(r, result);
    }

    private void execDVD(int r, int ix) {
        int divisor = regs.getGPRValue(ix);
        if (divisor != 0) {
            regs.setGPRValue(r, (regs.getGPRValue(r) / divisor) & 0xFFFF);
        }
    }

    private void execTRR(int r, int ix) {
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
        int addr = 0x200 + dev; // Device buffer area
        int value = memory.readForCPU(addr) & 0xFFFF;
        regs.setGPRValue(r, value);
        narrate(String.format("[IN] R%d ← dev=%d value=%04o", r, dev, value));
    }

    private void execOUT(int r, int dev) {
        int value = regs.getGPRValue(r) & 0xFF; // Only low byte
        narrate("[HALT] Program finished.");
    }

    private void execHLT() {
        halted = true;
    }
}
