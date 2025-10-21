package BasicMachine.simulator;

import BasicMachine.Memory_Module.Memory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/* ==============================================================
 * ProgramLoader.java
 * --------------------------------------------------------------
 * Purpose:
 *   Responsible for loading assembled machine code (.ld or .lst)
 *   into the Basic Machine’s memory for execution.
 *
 * Description:
 *   The ProgramLoader reads the assembler’s output file, interprets
 *   each line as an octal memory address and value pair, and stores
 *   them into Memory.
 *
 *   It then determines a suitable starting address for the Program Counter (PC),
 *   skipping over reserved data or zero padding at the beginning of memory.
 *
 * Integration:
 *   Used by:
 *     - SimulatorController.onIPL() (GUI: Initial Program Load)
 *     - MainSimulator.java (command-line execution)
 *
 *   Depends on:
 *     - Memory.java (for read/write and storage)
 *     - Assembler output (load.ld / output.lst)
 *
 * ==============================================================
 */
public class ProgramLoader {

    /* ==============================================================
     * KNOWN OPCODES
     * --------------------------------------------------------------
     * The set of operation codes currently recognized by the simulator.
     * Used to detect the first executable instruction in memory.
     *
     * These codes correspond to those defined in InstructionSet.java:
     *   1  = LDR
     *   2  = STR
     *   3  = LDX
     *   4  = LDA
     *   10 = JZ
     *   33 = HLT
     *
     * As more instructions are implemented, they can be added here.
     * ==============================================================
     */
    private static final Set<Integer> KNOWN_OPCODES = Set.of(1, 2, 3, 4, 10, 33);

    /* ==============================================================
     * load()
     * --------------------------------------------------------------
     * Reads a .ld or .lst file and loads its contents into memory.
     *
     * File format expected (octal values):
     *     <address> <value>
     *
     * Example:
     *     0010 102207
     *     0011 003412
     *     0012 020100
     *
     * Steps:
     *   1. Clear memory before loading
     *   2. Read file line-by-line
     *   3. Parse each address and instruction
     *   4. Store in memory
     *   5. Identify program start address (via heuristic)
     *
     * Parameters:
     *   file   — the .ld or .lst file to load
     *   memory — Memory object to load into
     *
     * Throws:
     *   Exception if file cannot be read or format is invalid
     * ==============================================================
     */
    public void load(File file, Memory memory) throws Exception {
        if (memory == null)
            throw new IllegalArgumentException("Memory is null");

        // Reset memory before loading new program
        memory.reset();

        int firstLoaded = -1;
        int lastLoaded  = -1;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String raw = strip(line).trim();
                if (raw.isEmpty()) continue;  // Skip comments and blanks

                String[] parts = raw.split("\\s+");
                if (parts.length < 2) continue;

                int addr = Integer.parseInt(parts[0], 8);
                int val  = Integer.parseInt(parts[1], 8);

                memory.writeWord(addr, val);

                if (firstLoaded == -1) firstLoaded = addr;
                if (addr > lastLoaded)  lastLoaded  = addr;
            }
        }

        // Determine a starting program address
        int start = (firstLoaded >= 0)
                ? findStart(memory, firstLoaded, lastLoaded)
                : 0;

        memory.setProgramStartAddress(start);
        System.out.printf("[IPL] Program start address set to %06o%n", start);
    }

    /* ==============================================================
     * findStart()
     * --------------------------------------------------------------
     * Determines the first address in memory that likely contains
     * the beginning of executable instructions.
     *
     * Heuristic:
     *   1. Prefer the first pair of *two consecutive known opcodes*.
     *      This helps skip data constants (like 002000).
     *   2. If no consecutive opcodes found, pick the first single
     *      known opcode.
     *   3. If no known opcodes exist, default to the first loaded word.
     *
     * Parameters:
     *   mem  — Memory object containing loaded program
     *   from — first loaded address
     *   to   — last loaded address
     *
     * Returns:
     *   starting address for the Program Counter (PC)
     * ==============================================================
     */
    private int findStart(Memory mem, int from, int to) {
        // Pass 1: require two consecutive known opcodes
        for (int a = from; a < to; a++) {
            if (isKnownOpcode(mem.readWord(a)) &&
                    isKnownOpcode(mem.readWord(a + 1))) {
                debugFound(a, mem);
                return a;
            }
        }

        // Pass 2: first single known opcode
        for (int a = from; a <= to; a++) {
            if (isKnownOpcode(mem.readWord(a))) {
                debugFound(a, mem);
                return a;
            }
        }

        // Fallback: first loaded address
        System.out.println("DEBUG: No plausible opcode run found; defaulting to "
                + Integer.toOctalString(from));
        return from;
    }

    /* ==============================================================
     * isKnownOpcode()
     * --------------------------------------------------------------
     * Extracts the 6-bit opcode from a 16-bit instruction word
     * and checks if it exists in the KNOWN_OPCODES set.
     * ==============================================================
     */
    private boolean isKnownOpcode(int word) {
        int opcode = (word >> 10) & 0x3F; // extract bits [15..10]
        return KNOWN_OPCODES.contains(opcode);
    }

    /* ==============================================================
     * debugFound()
     * --------------------------------------------------------------
     * Prints debugging info when a plausible start address is found.
     * ==============================================================
     */
    private void debugFound(int addr, Memory mem) {
        int w = mem.readWord(addr) & 0xFFFF;
        int opcode = (w >> 10) & 0x3F;
        System.out.printf("DEBUG: Start at %06o (opcode %d, word %06o)%n", addr, opcode, w);
    }

    /* ==============================================================
     * strip()
     * --------------------------------------------------------------
     * Removes inline comments from assembler output lines.
     * Recognizes:
     *   - ';'  (standard assembler comment)
     *   - '#'  (Python-style)
     *   - '//' (C/Java-style)
     * ==============================================================
     */
    private String strip(String s) {
        int cut = s.length();
        int a = s.indexOf(';');   if (a >= 0) cut = Math.min(cut, a);
        int b = s.indexOf('#');   if (b >= 0) cut = Math.min(cut, b);
        int c = s.indexOf("//");  if (c >= 0) cut = Math.min(cut, c);
        return s.substring(0, cut);
    }
}
