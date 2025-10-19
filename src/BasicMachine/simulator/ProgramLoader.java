package BasicMachine.simulator;

import BasicMachine.Memory_Module.Memory;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ProgramLoader {

    public void load(File file, Memory memory) throws Exception {
        if (memory == null) throw new IllegalArgumentException("Memory is null");
        memory.reset();

        int firstLoadedAddr = -1;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String raw = strip(line).trim();
                if (raw.isEmpty()) continue;

                // split by whitespace
                String[] parts = raw.split("\\s+");
                if (parts.length < 2) continue;

                // both address and value are OCTAL
                int addr = Integer.parseInt(parts[0], 8);
                int val  = Integer.parseInt(parts[1], 8);

                memory.writeWord(addr, val);

                if (firstLoadedAddr == -1)
                    firstLoadedAddr = addr;
            }
        }

        // Save first instruction (opcode != 0) as start address
        if (firstLoadedAddr >= 0) {
            int startAddr = findFirstInstruction(memory, firstLoadedAddr);
            memory.setProgramStartAddress(startAddr);
            System.out.printf("[IPL] Program start address set to %06o%n", startAddr);
        } else {
            throw new RuntimeException("No valid program data found in file!");
        }
    }

    private int findFirstInstruction(Memory memory, int fromAddr) {
        for (int i = fromAddr; i < memory.size(); i++) {
            int val = memory.readWord(i) & 0xFFFF;
            int opcode = (val >> 10) & 0x3F;  // extract 6-bit opcode
            if (opcode != 0) {
                return i; // First non-zero opcode found — start execution here
            }
        }
        // Fallback: if no valid instruction found, start from the first loaded address
        return fromAddr;
    }


    private String strip(String s) {
        int cut = s.length();
        int a = s.indexOf(';'); if (a >= 0) cut = Math.min(cut, a);
        int b = s.indexOf('#'); if (b >= 0) cut = Math.min(cut, b);
        int c = s.indexOf("//"); if (c >= 0) cut = Math.min(cut, c);
        return s.substring(0, cut);
    }
}
