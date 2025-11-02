package BasicMachine.simulator;

import BasicMachine.Memory_Module.Memory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ProgramLoader {
    private int programStartAddress = 0;

    // Add every opcode your CPU implements (decimal values of the top 6 bits)
    private static final Set<Integer> KNOWN_OPCODES = Set.of(
            // load/store/addressing
            0, 1, 2, 3, 33, 34,
            // arithmetic/immediate
            4, 5, 6, 7,
            // branches / control
            8, 9, 10, 11, 12, 13, 14, 15, 16,
            // reg-reg
            56, 57, 58, 59, 60, 61,
            // shift/rotate
            25, 26,
            // I/O
            49, 50
    );

    public void load(File file, Memory memory) throws Exception {
        if (memory == null) throw new IllegalArgumentException("Memory is null");
        memory.reset();

        int firstLoaded = -1, lastLoaded = -1;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String raw = strip(line);
                if (raw.isBlank()) continue;

                String[] parts = raw.trim().split("\\s+");
                if (parts.length < 2) continue;

                int addr = Integer.parseInt(parts[0], 8);
                int val  = Integer.parseInt(parts[1], 8);

                memory.writeWord(addr, val);

                if (firstLoaded == -1) firstLoaded = addr;
                if (lastLoaded  <  addr)          lastLoaded  = addr;
            }
        }

        if (firstLoaded < 0) {
            programStartAddress = 0;
        } else {
            programStartAddress = findStart(memory, firstLoaded, lastLoaded);
        }

        memory.setProgramStartAddress(programStartAddress);

        int word   = memory.readWord(programStartAddress) & 0xFFFF;
        int opcode = (word >> 10) & 0x3F;
        System.out.printf("[DEBUG] Start at %06o (opcode %d, word %06o)%n",
                programStartAddress, opcode, word);
    }

    // ======== start detection ========
    private int findStart(Memory mem, int from, int to) {
        // helper: 6-bit opcode
        java.util.function.IntUnaryOperator op = w -> ((w & 0xFFFF) >>> 10) & 0x3F;

        // Pass A: first address where we see a known opcode followed by another known
        // within the next 1–3 words. Also reject opcode==0 (HLT) as a starter.
        for (int a = from; a <= to; a++) {
            int o0 = op.applyAsInt(mem.readWord(a));
            if (o0 != 0 && KNOWN_OPCODES.contains(o0)) {
                for (int k = 1; k <= 3 && a + k <= to; k++) {
                    int ok = op.applyAsInt(mem.readWord(a + k));
                    if (ok != 0 && KNOWN_OPCODES.contains(ok)) {
                        return a;
                    }
                }
            }
        }

        // Pass B: first single known opcode (not 0)
        for (int a = from; a <= to; a++) {
            int o = op.applyAsInt(mem.readWord(a));
            if (o != 0 && KNOWN_OPCODES.contains(o)) return a;
        }

        // Pass C: first word that looks like an instruction (has higher bits set)
        for (int a = from; a <= to; a++) {
            int w = mem.readWord(a) & 0xFFFF;
            if (w >= 010000) return a;  // octal 10000: definitely not a tiny data literal
        }

        // Fallback: just start at the first loaded address
        return from;
    }

    private String strip(String s) {
        int cut = s.length();
        int a = s.indexOf(';');   if (a >= 0) cut = Math.min(cut, a);
        int b = s.indexOf('#');   if (b >= 0) cut = Math.min(cut, b);
        int c = s.indexOf("//");  if (c >= 0) cut = Math.min(cut, c);
        return s.substring(0, cut);
    }

    public int getProgramStartAddress() { return programStartAddress; }
}
