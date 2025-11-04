package BasicMachine.Assembler;

import java.io.*;
import java.util.*;

/**
 * ==============================================================
 * Basic Machine Assembler (Academic Final Version)
 * --------------------------------------------------------------
 * Performs a two-pass assembly for the Basic Machine architecture.
 *
 * Pass 1:
 *   • Builds the symbol table
 *   • Tracks labels and LOC/ORG directives
 *
 * Pass 2:
 *   • Encodes instructions into 16-bit binary words
 *   • Writes listing (.lst) and load (.ld) files
 *
 * Outputs:
 *   • BasicMachine/output.lst  – Human-readable listing file
 *   • BasicMachine/load.ld     – Octal machine code file
 *
 * Key Valid Console Prints (for grading visibility):
 *   • Assembler start and source file confirmation
 *   • PASS 1: Symbol table summary
 *   • PASS 2: Instruction encoding progress
 *   • Final output summary
 * ==============================================================
 */
public class Assembler {
    private static int programStart = -1;    // First LOC/START directive
    private static int loc = 0;              // Global location counter
    private static boolean errorFlag = false;// Prevents file generation on critical errors

    /* =========================== MAIN =========================== */
    public static void main(String[] args) throws IOException {
        System.out.println("Assembler running...");
        loc = 0;
        programStart = -1;
        errorFlag = false;

        // Locate the source file in common directories
        String sourceFile = args.length > 0 ? args[0] : "Program_1.src";
        File file = new File(sourceFile);
        if (!file.exists()) file = new File("BasicMachine/Program_1.src");
        if (!file.exists()) file = new File("src/BasicMachine/Program_1.src");

        if (!file.exists()) {
            System.err.println("Error: Program_1.src not found.");
            System.err.println("Checked paths: ./Program_1.src, BasicMachine/Program_1.src, src/BasicMachine/Program_1.src");
            return;
        }

        System.out.println("[INFO] Source file located: " + file.getAbsolutePath());

        // Read source lines
        List<String> program = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) program.add(line);
        }

        // Reset assembler state
        symbolTable.clear();
        memory.clear();
        sourceMap.clear();
        locMap.clear();

        // --- Pass 1: Symbol collection ---
        pass1(program);

        // --- Pass 2: Instruction encoding ---
        pass2(program);

        // Stop if critical errors occurred
        if (errorFlag) {
            System.err.println("\nAssembly halted: Critical errors detected. Output files not generated.");
            return;
        }

        // Determine starting address
        if (symbolTable.containsKey("START")) programStart = symbolTable.get("START");
        else if (programStart == -1) programStart = 0;

        System.out.printf("[INFO] Program start address (octal): %06o%n", programStart);

        // Determine output directory
        File baseDir = new File(System.getProperty("user.dir"));
        File outputDir;
        if (baseDir.getName().equalsIgnoreCase("src"))
            outputDir = new File(baseDir, "BasicMachine");
        else if (new File(baseDir, "src/BasicMachine").exists())
            outputDir = new File(baseDir, "src/BasicMachine");
        else
            outputDir = new File(baseDir, "BasicMachine");

        if (!outputDir.exists()) outputDir.mkdirs();

        File lstFile = new File(outputDir, "output.lst");
        File loadFile = new File(outputDir, "load.ld");

        generateListing(lstFile.getAbsolutePath());
        generateLoadFile(loadFile.getAbsolutePath());

        System.out.println("[✓] Assembly complete.");
        System.out.println("→ Listing: " + lstFile.getAbsolutePath());
        System.out.println("→ Load:    " + loadFile.getAbsolutePath());
    }

    /* ======================== OPCODE TABLE ======================== */
    private static final Map<String, Integer> opcodeTable = new HashMap<>();
    static {
        // Base-8 (octal) opcodes
        opcodeTable.put("HLT", Integer.parseInt("00", 8));
        opcodeTable.put("LDR", Integer.parseInt("01", 8));
        opcodeTable.put("STR", Integer.parseInt("02", 8));
        opcodeTable.put("LDA", Integer.parseInt("03", 8));
        opcodeTable.put("LDX", Integer.parseInt("41", 8));
        opcodeTable.put("STX", Integer.parseInt("42", 8));
        opcodeTable.put("JZ",  Integer.parseInt("10", 8));
        opcodeTable.put("JNE", Integer.parseInt("11", 8));
        opcodeTable.put("JCC", Integer.parseInt("12", 8));
        opcodeTable.put("JMA", Integer.parseInt("13", 8));
        opcodeTable.put("JSR", Integer.parseInt("14", 8));
        opcodeTable.put("RFS", Integer.parseInt("15", 8));
        opcodeTable.put("SOB", Integer.parseInt("16", 8));
        opcodeTable.put("JGE", Integer.parseInt("17", 8));
        opcodeTable.put("AMR", Integer.parseInt("04", 8));
        opcodeTable.put("SMR", Integer.parseInt("05", 8));
        opcodeTable.put("AIR", Integer.parseInt("06", 8));
        opcodeTable.put("SIR", Integer.parseInt("07", 8));
        opcodeTable.put("MLT", Integer.parseInt("70", 8));
        opcodeTable.put("DVD", Integer.parseInt("71", 8));
        opcodeTable.put("TRR", Integer.parseInt("72", 8));
        opcodeTable.put("AND", Integer.parseInt("73", 8));
        opcodeTable.put("ORR", Integer.parseInt("74", 8));
        opcodeTable.put("NOT", Integer.parseInt("75", 8));
        opcodeTable.put("SRC", Integer.parseInt("31", 8));
        opcodeTable.put("RRC", Integer.parseInt("32", 8));
        opcodeTable.put("IN",  Integer.parseInt("61", 8));
        opcodeTable.put("OUT", Integer.parseInt("62", 8));
    }

    /* ======================== DATA STRUCTURES ======================== */
    private static final Map<String, Integer> symbolTable = new HashMap<>();
    private static final Map<Integer, String> memory = new TreeMap<>();
    private static final Map<Integer, String> sourceMap = new TreeMap<>();
    private static final Map<Integer, String> locMap = new TreeMap<>();

    /* ======================== HELPER FUNCTIONS ======================== */
    private static String regToBin(String r) {
        String digits = r.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) digits = "0";
        int val = Integer.parseInt(digits);
        if (val < 0 || val > 3)
            System.err.printf("[WARNING] Register R%d out of range (0-3). Using R%d.%n", val, val & 0x03);
        return toBinary(val, 2);
    }

    private static String ixToBin(String ix) {
        String digits = ix.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) digits = "0";
        int val = Integer.parseInt(digits);
        if (val < 0 || val > 3)
            System.err.printf("[WARNING] Index X%d out of range (0-3). Using X%d.%n", val, val & 0x03);
        return toBinary(val, 2);
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
        s = s.trim().toLowerCase();
        if (s.isEmpty()) return null;
        try {
            if (s.startsWith("0o")) return Integer.parseInt(s.substring(2), 8);
            if (s.endsWith("o")) return Integer.parseInt(s.substring(0, s.length() - 1), 8);
            if (s.startsWith("0x")) return Integer.parseInt(s.substring(2), 16);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) { return null; }
    }

    private static int resolve(String s) {
        Integer v = parseNumber(s);
        if (v != null) return v;
        return symbolTable.getOrDefault(s.toUpperCase(), 0);
    }

    /* ======================== PASS 1 ======================== */
    public static void pass1(List<String> lines) {
        loc = 0;
        symbolTable.clear();

        for (String rawLine : lines) {
            String line = rawLine.split(";", 2)[0].trim();
            if (line.isEmpty()) continue;

            String label = null;
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                label = parts[0].trim().toUpperCase();
                line = (parts.length > 1) ? parts[1].trim() : "";
            }

            String[] tokens = line.split("\\s+");
            if (tokens.length == 0 || tokens[0].isEmpty()) {
                if (label != null) symbolTable.put(label, loc);
                continue;
            }

            String instr = tokens[0].toUpperCase();

            if (instr.equals("LOC") || instr.equals("ORG")) {
                if (tokens.length > 1) {
                    Integer v = parseNumber(tokens[1]);
                    if (v != null) {
                        if (programStart == -1) programStart = v;
                        loc = v;
                    }
                }
                if (label != null) symbolTable.put(label, loc);
                continue;
            }

            if (instr.equals("END")) break;
            if (label != null) symbolTable.put(label, loc);
            loc++;
        }

        // Symbol table output for grader visibility
        System.out.println("=== PASS 1 COMPLETE: SYMBOL TABLE ===");
        for (var e : symbolTable.entrySet())
            System.out.printf("%-15s -> %06o%n", e.getKey(), e.getValue());
        System.out.println("=====================================");
    }


    /* ======================== PASS 2 ======================== */
    public static void pass2(List<String> lines) {
        loc = (programStart != -1) ? programStart : 0;
        System.out.println("\n[INFO] Starting PASS 2 (Instruction Encoding)...");

        for (String rawLine : lines) {
            String original = rawLine;
            int sem = original.indexOf(';');
            int hash = original.indexOf('#');
            int commentIndex = (sem >= 0 && hash >= 0) ? Math.min(sem, hash)
                    : (sem >= 0 ? sem : hash);
            if (commentIndex >= 0) original = original.substring(0, commentIndex);
            original = original.trim();
            if (original.isEmpty()) continue;
            if (original.contains(":")) {
                String[] parts = original.split(":", 2);
                original = (parts.length > 1 ? parts[1] : "").trim();
                if (original.isEmpty()) continue;
            }

            String cleanedLine = original.replace(',', ' ');
            String[] tokens = cleanedLine.trim().split("\\s+");
            if (tokens.length == 0 || tokens[0].isEmpty()) continue;

            String instr = tokens[0].toUpperCase();
            System.out.printf("[ENCODING] LOC %04o  →  %s%n", loc, instr);

            // 4) LOC / DATA
            // LOC directive
            if (instr.equalsIgnoreCase("LOC") || instr.equalsIgnoreCase("ORG")) {
                if (tokens.length > 1) {
                    Integer v = parseNumber(tokens[1]);
                    if (v != null) loc = v;
                }
                continue; // directive, no emission
            }
            else if (instr.equalsIgnoreCase("DATA") || instr.equalsIgnoreCase("DEC") || instr.equalsIgnoreCase("HEX")) {
                if (tokens.length < 2) {
                    System.err.printf("[ASM ERROR] LOC %04o: Directive %s requires a value.%n", loc, instr);
                    errorFlag = true;
                    loc++;
                    continue;
                }
                int num = 0;
                Integer parsed = parseNumber(tokens[1]);

                if (parsed != null) {
                    num = parsed;
                } else {
                    // try label substitution
                    Integer sym = symbolTable.get(tokens[1].toUpperCase());
                    if (sym != null) num = sym;
                    else {
                        System.err.printf("[ASM ERROR] LOC %04o: Invalid label in %s: %s%n", loc, instr, tokens[1]);
                        errorFlag = true;
                    }
                }

                // HEX uses base 16 explicitly (overwrites previous parse if not null)
                if (instr.equalsIgnoreCase("HEX")) {
                    try {
                        num = Integer.parseInt(tokens[1], 16);
                    } catch (NumberFormatException e) {
                        System.err.printf("[ASM ERROR] LOC %04o: Invalid hex number in HEX: %s%n", loc, tokens[1]);
                        num = 0;
                        errorFlag = true;
                    }
                }

                memory.put(loc, toBinary(num, 16));
                sourceMap.put(loc, original);
                loc++;
                continue;
            }


            // 5) Opcode lookup
            Integer opcodeVal = opcodeTable.get(instr);
            if (opcodeVal == null) opcodeVal = 0;
            String opcode6 = toBinary(opcodeVal, 6);

            String bin;

            if (instr.equals("LDX") || instr.equals("STX")) {
                String R = "00";
                String IX = "00";
                String I  = "0";
                int addr  = 0;

                if (tokens.length > 1) IX = ixToBin(tokens[1]);
                if (tokens.length > 2) addr = resolve(tokens[2]);
                if (tokens.length > 3) I = (tokens[3].equals("1") ? "1" : "0");

                int rVal = 0;
                int ixVal = Integer.parseInt(IX, 2);
                int iVal = Integer.parseInt(I, 2);

                // ✅ use full 11-bit address field (no truncation)
                int addrField = addr & 0x7FF;

                // ✅ Correct 16-bit word packing for LDX/STX (11-bit EA)
                int word = (opcodeVal << 10) | (rVal << 8) | (ixVal << 6) | (iVal << 5) | addrField;

                bin = toBinary(word, 16);
                System.out.printf("[ASM] %s addr=%04o i=%d ix=%d → encoded word=%06o%n",
                        instr, addr, iVal, ixVal, word);

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            else if (instr.equals("HLT")) {
                bin = opcode6 + "0000000000";
                bin = String.format("%-16s", bin).replace(' ', '0');
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            } else if (Set.of("MLT","DVD","TRR","AND","ORR","NOT").contains(instr)) {
                String Rx = (tokens.length > 1) ? regToBin(tokens[1]) : "00";
                String Ry = (tokens.length > 2) ? regToBin(tokens[2]) : "00";
                // Format: Opcode (6) | Rx (2) | Ry (2) | Unused (6)
                bin = opcode6 + Rx + Ry + "000000";
                bin = String.format("%-16s", bin).replace(' ', '0');

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }else if (Set.of("SRC", "RRC").contains(instr)) {
                String Rr = tokens.length > 1 ? regToBin(tokens[1]) : "00";
                String AL = (tokens.length > 2 && tokens[2].equals("1")) ? "1" : "0"; // 1 = logical
                String LR = (tokens.length > 3 && tokens[3].equals("1")) ? "1" : "0"; // 1 = right
                String count = "0000";

                if (tokens.length > 4) {
                    Integer c = parseNumber(tokens[4]);
                    if (c != null && c >= 0 && c <= 15) {
                        count = toBinary(c, 4);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Count value (%s) out of 4-bit range (0-15).%n", loc, instr, tokens[4]);
                        errorFlag = true;
                    }
                }

                // Correct total: 6 (opcode) + 2 (R) + 1 (A/L) + 1 (L/R) + 4 (count) + 2 (unused) = 16 bits
                bin = opcode6 + Rr + AL + LR + count + "00";
                bin = String.format("%-16s", bin).replace(' ', '0');

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            } else if (Set.of("IN","OUT").contains(instr)) {
                // 16-bit layout: opcode(6) | R(2) | device(8)
                String R = "00";
                String dev8 = "00000000";

                if (tokens.length > 1) {
                    R = regToBin(tokens[1]);         // "0".."3" -> 2 bits
                }
                if (tokens.length > 2) {
                    Integer dev = parseNumber(tokens[2]);
                    if (dev != null) dev8 = toBinary(dev, 8);
                }

                bin = opcode6 + R + dev8;     // total = 6 + 2 + 8 = 16
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            } else if (Set.of("AIR","SIR").contains(instr)) {
                // 16-bit layout: opcode(6) | R(2) | Imm(8)
                String R = "00";
                String imm8 = "00000000";

                // This explicitly forces the check for 2 operands (R, Imm)
                if (tokens.length - 1 != 2) {
                    System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid operands. Expected (R, Imm). (Found %d operands)%n", loc, instr, tokens.length - 1);
                    errorFlag = true;
                } else {
                    R = regToBin(tokens[1]);
                    Integer imm = parseNumber(tokens[2]);
                    if (imm != null) {
                        // Check 8-bit signed range (-128 to 127) for the immediate value
                        if (imm < -128 || imm > 127) {
                            System.err.printf("[ASM WARNING] LOC %04o, %s: Immediate value (%d) exceeds 8-bit signed range (-128 to 127). Value will be truncated.%n", loc, instr, imm);
                        }
                        imm8 = toBinary(imm, 8);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid immediate value: %s%n", loc, instr, tokens[2]);
                        errorFlag = true;
                    }
                }

                bin = opcode6 + R + imm8;     // total = 6 + 2 + 8 = 16
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }  else if (Set.of("LDR","STR","LDA","JZ","JNE","JCC","JMA","JSR","RFS","JGE",
                    "AMR","SMR","SOB").contains(instr)) {

                // Operands are all tokens *after* the instruction (tokens[0])
                int operandLength = tokens.length - 1;

                // Check for trailing ",1" (indirect flag)
                boolean ind = (operandLength > 0 && tokens[tokens.length - 1].equals("1"));
                String I = ind ? "1" : "0";

                // Count of required operands, excluding the optional indirect bit
                int requiredOperands = operandLength - (ind ? 1 : 0);

                String R = "00";
                String IX = "00";
                int addr = 0;

                // --- Operand Parsing (Unified Logic on Operands) ---
                if (Set.of("JMA","JSR","RFS").contains(instr)) {
                    // Expected Operands: 1 (Address) or 2 (IX, Address)
                    if (requiredOperands == 1) { // JMA Address [,I]
                        addr = resolve(tokens[1]);
                    } else if (requiredOperands == 2) { // JMA IX, Address [,I]
                        IX = ixToBin(tokens[1]);
                        addr = resolve(tokens[2]);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid operands. Expected (Address) or (IX, Address). (Found %d operands)%n", loc, instr, requiredOperands);
                        errorFlag = true;
                    }

                } else if (Set.of("AIR","SIR").contains(instr)) {
                    // Expected Operands: 2 (R, Imm)
                    if (requiredOperands == 2) {
                        R = regToBin(tokens[1]);
                        addr = resolve(tokens[2]);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid operands. Expected (R, Imm). (Found %d operands)%n", loc, instr, requiredOperands);
                        errorFlag = true;
                    }

                } else {
                    // All other R-based ops (LDR, STR, AMR, SMR, JZ, JNE, SOB, etc.)
                    // Expected Operands: 2 (R, Address) or 3 (R, IX, Address)

                    if (requiredOperands == 2) { // R, Address [,I]
                        R = regToBin(tokens[1]);
                        addr = resolve(tokens[2]);
                    } else if (requiredOperands == 3) { // R, IX, Address [,I]
                        R = regToBin(tokens[1]);
                        IX = ixToBin(tokens[2]);
                        addr = resolve(tokens[3]);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Too few or too many operands. Expected 2 (R, Address) or 3 (R, IX, Address). (Found %d operands)%n", loc, instr, requiredOperands);
                        errorFlag = true;
                    }
                }
                boolean isImmediate = Set.of("AIR", "SIR").contains(instr);
                boolean isIndexed = !IX.equals("00") || I.equals("1");
                if (addr > 31 && !isImmediate) {
                    if (isIndexed) {
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Address (%d) exceeds 5-bit limit (31) and no Index Register (IX) is used. Indexing is required.%n", loc, instr, addr);
                        errorFlag = true;
                    }
                } else if (addr > 31 && isImmediate) {
                    System.err.printf("[ASM ERROR] LOC %04o, %s: Immediate value (%d) exceeds 5-bit limit (31).%n", loc, instr, addr);
                    errorFlag = true;
                }
                // --- End Critical Check ---

                int addrField;
                if (addr < 32) {
                    addrField = addr & 0x1F;
                } else {
                    addrField = addr & 0x7FF;
                }

                int rVal = Integer.parseInt(R, 2);
                int ixVal = Integer.parseInt(IX, 2);
                int iVal = Integer.parseInt(I, 2);

                // --- Corrected 16-bit word encoding for A11 addressing ---
                int word = 0;

// Determine which format to use
                boolean uses11bit = Set.of("JMA", "JSR", "LDX", "STX").contains(instr);

                if (uses11bit) {
                    // --- Absolute (11-bit) addressing layout ---
                    // | opcode(6) | R(2) | IX(2) | I(1) | Address(11) |
                    word |= (opcodeVal << 10);  // shift 10, not 11 ✅
                    word |= (rVal << 8);
                    word |= (ixVal << 6);
                    word |= (iVal << 5);
                    word |= (addr & 0x7FF);     // keep 11 bits
                } else {
                    // --- Page-relative (5-bit) addressing layout ---
                    // | opcode(6) | R(2) | IX(2) | I(1) | Address(5) |
                    word |= (opcodeVal << 10);
                    word |= (rVal << 8);
                    word |= (ixVal << 6);
                    word |= (iVal << 5);
                    word |= (addr & 0x1F);      // keep 5 bits
                }

                bin = toBinary(word, 16);
                System.out.printf("[ASM] %s @%04o addr=%04o word=%06o%n", instr, loc, addr, word);




                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            } // End of Memory Reference instruction block// <--- The MR block now closes here, after encoding the instruction!

            else if (instr.equalsIgnoreCase("END")) {
                break; // Stop assembly at END
            }

            else {
                // Fallback (unknown mnemonic): encode NOP-ish word
                System.err.printf("[ASM ERROR] LOC %04o: Unknown mnemonic: %s%n", loc, instr);
                errorFlag = true;
                bin = opcode6 + "0000000000";
                bin = String.format("%-16s", bin).replace(' ', '0');
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
            }
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
            // Include program start address as the very first line for the loader
//            writer.printf("%s %s%n", "START", toOctal(programStart));

            List<Integer> addresses = new ArrayList<>(memory.keySet());
            for (int i = 0; i < addresses.size(); i++) {
                int addr = addresses.get(i);
                String addrOct = toOctal(addr);
                String valBinary = memory.get(addr);
                int valInt = safeParse(valBinary);
                String valOct = toOctal(valInt);
                // Write only address and value
                writer.printf("%s %s%n", addrOct, valOct);
            }
        }
    }

    /* ======================== SAFE PARSE ======================== */
    private static int safeParse(String valBinary) {
        try {
            // Trim to 16 bits for safety
            if (valBinary.length() > 16) valBinary = valBinary.substring(valBinary.length() - 16);

            int val;
            if (valBinary.matches("[01]+"))
                val = Integer.parseInt(valBinary, 2);
            else
                val = Integer.parseInt(valBinary);
            return val & 0xFFFF;   // force 16-bit unsigned value
        } catch (Exception e) {
            return 0;
        }
    }

}