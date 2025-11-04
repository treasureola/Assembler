package BasicMachine.simulator;

import BasicMachine.Memory_Module.Memory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * ==============================================================
 * ProgramLoader.java (final stable version)
 * --------------------------------------------------------------
 * Loads an assembled .ld file into memory and finds the first
 * executable instruction (skipping leading data words).
 * ==============================================================
 */
public class ProgramLoader {
    private int programStartAddress = 0;

    // Recognized opcodes (decimal value of 6-bit opcode)
    private static final Set<Integer> KNOWN_OPCODES = Set.of(
            // load/store (1, 2, 3, 4 are LDR, STR, LDA, ?? )
            1, 2, 3, 4,
            // LDX (41 octal = 33 decimal)
            33,
            // STX (42 octal = 34 decimal) <- ADD THIS
            34,
            // arithmetic/immediate
            5, 6, 7,
            // branches / control
            8, 9, 10, 11, 12, 13, 14, 15, 16,
            // logical / reg-reg
            56, 57, 58, 59, 60, 61,
            // shift / rotate
            25, 26,
            // I/O
            49, 50
    );

    /**
     * Loads the program into memory.
     */
    public void load(File file, Memory memory) throws Exception {
//        memory.disableCache();  // ✅ Bypass cache for program loading
        memory.enableCache();
        if (memory == null)
            throw new IllegalArgumentException("Memory is null");
        memory.reset();

        int firstLoaded = -1, lastLoaded = -1;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String raw = strip(line).trim();
                if (raw.isEmpty()) continue;

                String[] parts = raw.split("\\s+");
                if (parts.length < 2) continue;

                int addr = Integer.parseInt(parts[0], 8);
                int val = Integer.parseInt(parts[1], 8);

                memory.writeWord(addr, val);
                if (firstLoaded == -1) firstLoaded = addr;
                if (addr > lastLoaded) lastLoaded = addr;
            }
        }

        if (firstLoaded < 0) {
            programStartAddress = 0;
        } else {
            programStartAddress = findStart(memory, firstLoaded, lastLoaded);
        }

        memory.setProgramStartAddress(programStartAddress);

        int word = memory.readWord(programStartAddress) & 0xFFFF;
        int opcode = (word >> 10) & 0x3F;
        System.out.printf("[DEBUG] Start at %06o (opcode %d, word %06o)%n",
                programStartAddress, opcode, word);
    }

    /**
     * Heuristic: find first address that likely holds code.
     */
    private int findStart(Memory mem, int from, int to) {
        java.util.function.IntUnaryOperator op = w -> ((w & 0xFFFF) >>> 10) & 0x3F;

        // Pass 1: find first known opcode that’s not 0 (HLT at start is invalid)
        for (int a = from; a <= to; a++) {
            int code = op.applyAsInt(mem.readWord(a));
            if (code != 0 && KNOWN_OPCODES.contains(code)) {
                return a;
            }
        }

        // Pass 2: fallback — find first word with high bits set (>= octal 010000)
        for (int a = from; a <= to; a++) {
            int w = mem.readWord(a) & 0xFFFF;
            if (w >= 010000) return a;
        }

        // Pass 3: ultimate fallback
        System.out.println("DEBUG: No plausible opcode found; defaulting to " +
                Integer.toOctalString(from));
        return from;
    }

    /** Removes inline comments and garbage text */
    private String strip(String s) {
        int cut = s.length();
        int a = s.indexOf(';');   if (a >= 0) cut = Math.min(cut, a);
        int b = s.indexOf('#');   if (b >= 0) cut = Math.min(cut, b);
        int c = s.indexOf("//");  if (c >= 0) cut = Math.min(cut, c);
        return s.substring(0, cut);
    }

    public int getProgramStartAddress() { return programStartAddress; }
}
