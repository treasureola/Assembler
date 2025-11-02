package BasicMachine.Assembler;

import java.io.*;
import java.util.*;

/**
 * ==============================================================
 *  Basic Machine Assembler (Final, Fixed Version)
 * --------------------------------------------------------------
 *  - Uses base-8 (octal) opcodes per architecture spec.
 *  - Always outputs to the real BasicMachine folder, regardless
 *    of where it’s run from (src/, out/, IntelliJ, or JAR).
 *  - Generates:
 *       BasicMachine/output.lst  (human-readable)
 *       BasicMachine/load.ld     (machine load file)
 * ==============================================================
 */
public class Assembler {
    // Track the first LOC directive as the start address for IPL
    private static int programStart = -1;

    /* =========================== MAIN =========================== */
    public static void main(String[] args) throws IOException {
        System.out.println("Assembler running...");


        // Locate source file
        String sourceFile = args.length > 0 ? args[0] : "Program_1.src";
        File file = new File(sourceFile);
        if (!file.exists()) file = new File("BasicMachine/Program_1.src");
        if (!file.exists()) file = new File("src/BasicMachine/Program_1.src");

        if (!file.exists()) {
            System.err.println("❌ Error: Program_1.src not found.");
            System.err.println("Tried: ./Program_1.src, BasicMachine/Program_1.src, src/BasicMachine/Program_1.src");
            return;
        }

        System.out.println("[✓] Found source file: " + file.getAbsolutePath());

        // Read all source lines
        List<String> program = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                program.add(line);
            }
        }

        // Run two-pass assembly
        pass1(program);
        pass2(program);

        // === FIXED OUTPUT DIRECTORY HANDLING ===
        File baseDir = new File(System.getProperty("user.dir"));
        File outputDir;

        if (baseDir.getName().equalsIgnoreCase("src")) {
            outputDir = new File(baseDir, "BasicMachine");
        } else if (new File(baseDir, "src/BasicMachine").exists()) {
            outputDir = new File(baseDir, "src/BasicMachine");
        } else {
            outputDir = new File(baseDir, "BasicMachine");
        }

        if (!outputDir.exists()) outputDir.mkdirs();

        File lstFile = new File(outputDir, "output.lst");
        File loadFile = new File(outputDir, "load.ld");

        generateListing(lstFile.getAbsolutePath());
        generateLoadFile(loadFile.getAbsolutePath());

        System.out.println("[✓] Assembly complete.");
        System.out.println("→ Listing written to: " + lstFile.getAbsolutePath());
        System.out.println("→ Load file written to: " + loadFile.getAbsolutePath());
    }

    /* ======================== OPCODE TABLE ======================== */
    private static final Map<String, Integer> opcodeTable = new HashMap<>();
    static {
        // Base-8 (octal) opcodes per project specification
        opcodeTable.put("HLT", 00);
        opcodeTable.put("LDR", 01);
        opcodeTable.put("STR", 02);
        opcodeTable.put("LDA", 03);

        opcodeTable.put("LDX", 041);
        opcodeTable.put("STX", 042);

        opcodeTable.put("JZ", 010);
        opcodeTable.put("JNE", 011);
        opcodeTable.put("JCC", 012);
        opcodeTable.put("JMA", 013);
        opcodeTable.put("JSR", 014);
        opcodeTable.put("RFS", 015);
        opcodeTable.put("SOB", 016);
        opcodeTable.put("JGE", 017);

        opcodeTable.put("AMR", 04);
        opcodeTable.put("SMR", 05);
        opcodeTable.put("AIR", 06);
        opcodeTable.put("SIR", 07);

        opcodeTable.put("MLT", 070);
        opcodeTable.put("DVD", 071);
        opcodeTable.put("TRR", 072);
        opcodeTable.put("AND", 073);
        opcodeTable.put("ORR", 074);
        opcodeTable.put("NOT", 075);

        opcodeTable.put("SRC", 031);
        opcodeTable.put("RRC", 032);

        opcodeTable.put("IN", 061);
        opcodeTable.put("OUT", 062);
    }

    /* ======================== DATA STRUCTURES ======================== */
    private static final Map<String, Integer> symbolTable = new HashMap<>();
    private static final Map<Integer, String> memory = new TreeMap<>();
    private static final Map<Integer, String> sourceMap = new TreeMap<>();
    private static final Map<Integer, String> locMap = new TreeMap<>();

    /* ======================== HELPERS ======================== */
    private static String regToBin(String r) {
        String digits = r.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) digits = "0";
        return toBinary(Integer.parseInt(digits), 2);
    }

    private static String ixToBin(String ix) {
        String digits = ix.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) digits = "0";
        return toBinary(Integer.parseInt(digits), 2);
    }

    private static String toBinary(int value, int bits) {
        int mask = (1 << bits) - 1;
        String bin = Integer.toBinaryString(value & mask);
        return String.format("%" + bits + "s", bin).replace(' ', '0');
    }

    private static String toOctal(int value) {
        return String.format("%06o", value & 0xFFFF);
    }

    private static Integer parseNumber(String s) {
        s = s.trim();
        try {
            if (s.startsWith("0x") || s.startsWith("0X"))
                return Integer.parseInt(s.substring(2), 16);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* ======================== PASS 1 ======================== */
    public static void pass1(List<String> lines) {
        int loc = 0;
        for (String rawLine : lines) {
            String line = rawLine.split(";", 2)[0].trim();
            if (line.isEmpty()) continue;

            String label = null;
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                label = parts[0].trim().toUpperCase();
                line = parts.length > 1 ? parts[1].trim() : "";
            }

            if (!line.isEmpty()) {
                String[] tokens = line.split("\\s+");
                String instr = tokens[0].toUpperCase();

                if (instr.equals("LOC") && tokens.length > 1) {
                    loc = Integer.parseInt(tokens[1]);
                    if (label != null) symbolTable.put(label, loc);
                } else {
                    if (label != null) symbolTable.put(label, loc);
                    loc++;
                }
            } else if (label != null) symbolTable.put(label, loc);
        }
    }

    /* ======================== PASS 2 ======================== */
    public static void pass2(List<String> lines) {
        int loc = 0;

        for (String rawLine : lines) {
            // Keep the original source line for listing
            String original = rawLine;

            // 1) Strip comment-only (“#…”) and ";" trailers
            String line = rawLine;
            if (line.trim().startsWith("#")) continue;
            int sem = line.indexOf(';');
            if (sem >= 0) line = line.substring(0, sem);
            line = line.trim();
            if (line.isEmpty()) continue;

            // 2) Remove label part if present
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                line = (parts.length > 1 ? parts[1] : "").trim();
                if (line.isEmpty()) continue;
            }

            // 3) Tokenize
            String[] tokens = line.split("[,\\s]+");
            String instr = tokens[0].toUpperCase();

            // 4) LOC / DATA
            // LOC directive
            if (instr.equals("LOC")) {
                loc = Integer.parseInt(tokens[1]);
                if (programStart == -1) programStart = loc;  // <-- record start
                continue;
            }
            if (instr.equals("DATA")) {
                int num = 0;
                Integer parsed = parseNumber(tokens[1]);
                if (parsed != null) num = parsed;
                // Store data as 16-bit binary so the writers can parse it safely
                memory.put(loc, toBinary(num, 16));
                sourceMap.put(loc, original);
                System.out.printf("[ASM DEBUG] LOC=%04o | DATA  | BIN=%s%n", loc, toBinary(num,16));
                loc++;
                continue;
            }

            // 5) Opcode lookup (octal table → numeric → 6-bit binary)
            Integer opcodeVal = opcodeTable.get(instr);
            System.out.printf("[DEBUG OPCODE] %-5s = %d (octal %03o)%n", instr, opcodeVal, opcodeVal);
            if (opcodeVal == null) opcodeVal = 0;
            String opcode6 = toBinary(opcodeVal, 6);

            String bin;

        /* -------- SPECIAL CASE: LDX / STX --------
           Encoding: opcode(6) | R(2=00) | IX(2 = first operand) | I(1) | addr5
           Example: LDX 1,7   → R=00, IX=01, I=0, addr=00111
        */
            if (instr.equals("LDX") || instr.equals("STX")) {
                String R = "00";
                String IX = "00";
                String I  = "0";
                int addr  = 0;

                if (tokens.length > 1) IX = ixToBin(tokens[1]);  // IX from first operand
                if (tokens.length > 2) {
                    Integer val = parseNumber(tokens[2]);
                    addr = (val != null) ? val : symbolTable.getOrDefault(tokens[2].toUpperCase(), 0);
                }
                if (tokens.length > 3) I = (tokens[3].equals("1") ? "1" : "0");

                int addr5 = addr & 0x1F;
                int currPage = (loc & ~0x1F);
                if ((addr & ~0x1F) != currPage) {
                    System.out.printf(
                            "[ASM WARN] Cross-page LDX/STX at LOC %04o: requested %04o, encoding %02o (low 5 bits only).%n",
                            loc, addr, addr5
                    );
                }
                String addrBits = toBinary(addr5, 5);

                bin = opcode6 + R + IX + I + addrBits;

                // 16-bit pad
                if (bin.length() > 16) bin = bin.substring(0, 16);
                else bin = String.format("%-16s", bin).replace(' ', '0');

                System.out.printf("[ASM DEBUG] LOC=%04o | %-5s | BIN=%s%n", loc, instr, bin);
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            /* -------- HALT -------- */
            if (instr.equals("HLT")) {
                bin = opcode6 + "0000000000";
                bin = String.format("%-16s", bin).replace(' ', '0');
                System.out.printf("[ASM DEBUG] LOC=%04o | HLT   | BIN=%s%n", loc, bin);
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

        /* -------- Register-to-Register group --------
           Enc: opcode(6) | Rx(2) | Ry(2) | 000000
           (MLT, DVD, TRR, AND, ORR, NOT)
        */
            if (Set.of("MLT","DVD","TRR","AND","ORR","NOT").contains(instr)) {
                String Rx = (tokens.length > 1) ? regToBin(tokens[1]) : "00";
                String Ry = (tokens.length > 2) ? regToBin(tokens[2]) : "00";
                bin = opcode6 + Rx + Ry + "000000";
                bin = String.format("%-16s", bin).replace(' ', '0');

                System.out.printf("[ASM DEBUG] LOC=%04o | %-5s | BIN=%s%n", loc, instr, bin);
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

        /* -------- Shifts / Rotates (SRC, RRC)
           Your spec variant (as you already used):
           opcode(6) | R(2) | A/L(1) | L/R(1) | count(4) | 0000
        */
            else if (Set.of("SRC", "RRC").contains(instr)) {
                String Rr = tokens.length > 1 ? regToBin(tokens[1]) : "00";
                String AL = (tokens.length > 2 && tokens[2].equals("1")) ? "1" : "0"; // 1 = logical
                String LR = (tokens.length > 3 && tokens[3].equals("1")) ? "1" : "0"; // 1 = right
                String count = (tokens.length > 4) ? toBinary(Integer.parseInt(tokens[4]), 4) : "0000";

                // Correct total: 6 (opcode) + 2 (R) + 1 (A/L) + 1 (L/R) + 4 (count) + 2 (unused) = 16 bits
                bin = opcode6 + Rr + AL + LR + count + "00";
            }



        /* -------- I/O (IN, OUT)
           Enc: opcode(6) | R(2) | device(8)
        */
            if (Set.of("IN","OUT").contains(instr)) {
                String R = (tokens.length > 1) ? regToBin(tokens[1]) : "00";
                int dev = (tokens.length > 2) ? Integer.parseInt(tokens[2]) : 0;
                String dev8 = toBinary(dev, 8);
                bin = opcode6 + R + dev8;
                bin = String.format("%-16s", bin).replace(' ', '0');

                System.out.printf("[ASM DEBUG] LOC=%04o | %-5s | BIN=%s%n", loc, instr, bin);
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

        /* -------- Load/Store/Addr/Branches/Immediate --------
           Enc: opcode(6) | R(2) | IX(2) | I(1) | addr5(5)
           (LDR, STR, LDA, JZ, JNE, JCC, JMA, JSR, RFS, SOB, JGE,
            AMR, SMR, AIR, SIR)
        */
            if (Set.of("LDR","STR","LDA","JZ","JNE","JCC","JMA","JSR","RFS","SOB","JGE",
                    "AMR","SMR","AIR","SIR").contains(instr)) {
                String R = (tokens.length > 1) ? regToBin(tokens[1]) : "00";
                String IX = (tokens.length > 2) ? ixToBin(tokens[2]) : "00";
                String I  = "0";
                int addr  = 0;

                if (tokens.length > 3) {
                    Integer val = parseNumber(tokens[3]);
                    addr = (val != null) ? val : symbolTable.getOrDefault(tokens[3].toUpperCase(), 0);
                }
                if (tokens.length > 4) I = (tokens[4].equals("1") ? "1" : "0");

                int addr5 = addr & 0x1F;
                int currPage = (loc & ~0x1F);
                if ((addr & ~0x1F) != currPage) {
                    System.out.printf(
                            "[ASM WARN] Cross-page at LOC %04o for %-5s: requested %04o, encoding %02o (low 5 bits).%n",
                            loc, instr, addr, addr5
                    );
                }
                String addrBits = toBinary(addr5, 5);

                bin = opcode6 + R + IX + I + addrBits;
                bin = String.format("%-16s", bin).replace(' ', '0');

                System.out.printf("[ASM DEBUG] LOC=%04o | %-5s | BIN=%s%n", loc, instr, bin);
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            // Fallback (unknown mnemonic): encode NOP-ish word
            bin = opcode6 + "0000000000";
            bin = String.format("%-16s", bin).replace(' ', '0');
            System.out.printf("[ASM DEBUG] LOC=%04o | %-5s | BIN=%s (UNKNOWN → padded)%n", loc, instr, bin);
            memory.put(loc, bin);
            sourceMap.put(loc, original);
            loc++;
        }
    }



    /* ======================== FILE GENERATION ======================== */
    public static void generateListing(String outFile) throws IOException {
        System.out.println("[DEBUG] Writing listing file to " + outFile);
        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
            for (int addr : new TreeSet<>(memory.keySet())) {
                String addrOct = toOctal(addr);
                String valBinary = memory.get(addr);
                int valInt = safeParse(valBinary);
                String valOct = toOctal(valInt);
                writer.printf("%s %s %s%n", addrOct, valOct, sourceMap.getOrDefault(addr, ""));
            }
        }
    }

    public static void generateLoadFile(String outFile) throws IOException {
        System.out.println("[DEBUG] Writing load file to " + outFile);
        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
            List<Integer> addresses = new ArrayList<>(memory.keySet());
            for (int i = 0; i < addresses.size(); i++) {
                int addr = addresses.get(i);
                String addrOct = toOctal(addr);
                String valBinary = memory.get(addr);
                int valInt = safeParse(valBinary);
                String valOct = toOctal(valInt);
                // Write only address and value, no headers
                writer.printf("%s %s%n", addrOct, valOct);
            }
        }
    }



    /* ======================== SAFE PARSE ======================== */
    private static int safeParse(String valBinary) {
        try {
            if (valBinary.matches("[01]+"))
                return Integer.parseInt(valBinary, 2);
            return Integer.parseInt(valBinary);
        } catch (Exception e) {
            return 0;
        }
    }
}
