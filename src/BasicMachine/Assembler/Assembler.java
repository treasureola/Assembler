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
 * Outputs (for this project setup):
 *   • <PROJECT_ROOT>/output.lst  – Human-readable listing file
 *   • <PROJECT_ROOT>/load.ld     – Octal machine code file
 *
 * Where <PROJECT_ROOT> is the directory that contains:
 *   - run_all.sh
 *   - src/
 *   - out/
 *
 * This is derived from the source file path:
 *   .../Assembler/src/BasicMachine/Program2_test.src
 * ==============================================================
 */
public class Assembler {
    private static int programStart = -1;    // First LOC/START directive
    private static int loc = 0;              // Global location counter
    private static boolean errorFlag = false;// Prevents file generation on critical errors
    private static boolean quietMode = false;

    /* =========================== MAIN =========================== */
    /* =========================== MAIN =========================== */
    public static void main(String[] args) throws IOException {
        // Reset state each run
        loc = 0;
        programStart = -1;
        errorFlag = false;
        quietMode = false;

        // ----------------------------------------------
        // 1) Parse args: [sourceFile] [--quiet]
        // ----------------------------------------------
        String sourceFile = null;
        for (String arg : args) {
            if ("--quiet".equalsIgnoreCase(arg)) {
                quietMode = true;
            } else if (sourceFile == null) {
                sourceFile = arg;
            }
        }

        // Default if no file explicitly given
        if (sourceFile == null) {
            sourceFile = "src/BasicMachine/Program2_test.src";
        }

        if (!quietMode) {
            System.out.println("Assembler running...");
        }

        File file = new File(sourceFile);
        String baseName = new File(sourceFile).getName();

// Try as-is first (works when run from project root, IntelliJ, etc.)
        if (!file.exists()) file = new File("src/BasicMachine/" + baseName);
        if (!file.exists()) file = new File("BasicMachine/" + baseName);
        if (!file.exists()) file = new File(baseName);

        if (!file.exists()) {
            System.err.println("Error: source file not found: " + sourceFile);
            System.err.println("Checked paths:");
            System.err.println("  " + new File("src/BasicMachine/" + baseName).getPath());
            System.err.println("  " + new File("BasicMachine/" + baseName).getPath());
            System.err.println("  " + new File(baseName).getPath());
            return;
        }

        file = file.getAbsoluteFile();
        if (!quietMode) {
            System.out.println("[INFO] Source file located: " + file.getAbsolutePath());
        }

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

        // Determine starting address
        if (symbolTable.containsKey("START")) {
            programStart = symbolTable.get("START");
        } else if (programStart == -1) {
            programStart = 0;
        }

        if (!quietMode) {
            System.out.printf("[INFO] Program start address (octal): %06o%n", programStart);
        }

        // ---------- Output file placement ----------
        File cwd = new File(System.getProperty("user.dir"));
        File projectRoot = cwd;

        if ("out".equalsIgnoreCase(cwd.getName())) {
            projectRoot = cwd.getParentFile();
        }
        if (projectRoot == null) {
            projectRoot = cwd;
        }

        File lstFile  = new File(projectRoot, "output.lst");
        File loadFile = new File(projectRoot, "load.ld");

        generateListing(lstFile.getAbsolutePath());
        generateLoadFile(loadFile.getAbsolutePath());

        if (!quietMode) {
            System.out.println("[✓] Assembly complete.");
            System.out.println("→ Listing: " + lstFile.getAbsolutePath());
            System.out.println("→ Load:    " + loadFile.getAbsolutePath());
        }
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
        opcodeTable.put("CHK", Integer.parseInt("63", 8)); // device check
        opcodeTable.put("TRAP",Integer.parseInt("24", 8)); // trap
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

        if (val < 0 || val > 3) {
            // In quiet mode, don't spam warnings
            if (!quietMode) {
                System.err.printf(
                        "[WARNING] Register R%d out of range (0-3). Using R%d.%n",
                        val, (val & 0x03)
                );
            }
            val = val & 0x03; // mask down to valid 0–3
        }

        return toBinary(val, 2);
    }


    private static String ixToBin(String ix) {
        String digits = ix.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) digits = "0";
        int val = Integer.parseInt(digits);

        if (val < 0 || val > 3) {
            if (!quietMode) {
                System.err.printf(
                        "[WARNING] Index X%d out of range (0-3). Using X%d.%n",
                        val, (val & 0x03)
                );
            }
            val = val & 0x03;
        }

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

        if (!quietMode) {
            System.out.println("=== PASS 1 COMPLETE: SYMBOL TABLE ===");
            for (var e : symbolTable.entrySet())
                System.out.printf("%-15s -> %06o%n", e.getKey(), e.getValue());
            System.out.println("=====================================");
        }
    }


    /* ======================== PASS 2 ======================== */
    public static void pass2(List<String> lines) {
        loc = (programStart != -1) ? programStart : 0;
//        System.out.println("\n[INFO] Starting PASS 2 (Instruction Encoding)...");

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
//            System.out.printf("[ENCODING] LOC %04o  →  %s%n", loc, instr);

            // LOC / ORG directives
            if (instr.equalsIgnoreCase("LOC") || instr.equalsIgnoreCase("ORG")) {
                if (tokens.length > 1) {
                    Integer v = parseNumber(tokens[1]);
                    if (v != null) loc = v;
                }
                continue; // directive, no emission
            }
            // DATA/DEC/HEX directives
            else if (instr.equalsIgnoreCase("DATA") || instr.equalsIgnoreCase("DEC") || instr.equalsIgnoreCase("HEX")) {
                if (tokens.length < 2) {
//                    System.err.printf("[ASM ERROR] LOC %04o: Directive %s requires a value.%n", loc, instr);
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
//                        System.err.printf("[ASM ERROR] LOC %04o: Invalid label in %s: %s%n", loc, instr, tokens[1]);
                        errorFlag = true;
                    }
                }

                if (instr.equalsIgnoreCase("HEX")) {
                    try {
                        num = Integer.parseInt(tokens[1], 16);
                    } catch (NumberFormatException e) {
//                        System.err.printf("[ASM ERROR] LOC %04o: Invalid hex number in HEX: %s%n", loc, tokens[1]);
                        num = 0;
                        errorFlag = true;
                    }
                }

                memory.put(loc, toBinary(num, 16));
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            // Opcode lookup
            Integer opcodeVal = opcodeTable.get(instr);
            if (opcodeVal == null) opcodeVal = 0;
            String opcode6 = toBinary(opcodeVal, 6);

            String bin;

            // -------- LDX/STX (11-bit addressing) --------
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
                int addrField = addr & 0x7FF;

                int word = (opcodeVal << 10) | (rVal << 8) | (ixVal << 6) | (iVal << 5) | addrField;

                bin = toBinary(word, 16);
//                System.out.printf("[ASM] %s addr=%04o i=%d ix=%d → encoded word=%06o%n",
//                        instr, addr, iVal, ixVal, word);

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            // HLT
            else if (instr.equals("HLT")) {
                bin = opcode6 + "0000000000";
                bin = String.format("%-16s", bin).replace(' ', '0');
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // Register-Register ops: MLT, DVD, TRR, AND, ORR, NOT
            else if (Set.of("MLT","DVD","TRR","AND","ORR","NOT").contains(instr)) {
                String Rx = (tokens.length > 1) ? regToBin(tokens[1]) : "00";
                String Ry = (tokens.length > 2) ? regToBin(tokens[2]) : "00";
                bin = opcode6 + Rx + Ry + "000000";
                bin = String.format("%-16s", bin).replace(' ', '0');

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // Shift / Rotate: SRC, RRC
            else if (Set.of("SRC", "RRC").contains(instr)) {
                String Rr = tokens.length > 1 ? regToBin(tokens[1]) : "00";
                String AL = (tokens.length > 2 && tokens[2].equals("1")) ? "1" : "0"; // 1 = logical
                String LR = (tokens.length > 3 && tokens[3].equals("1")) ? "1" : "0"; // 1 = right
                String count = "0000";

                if (tokens.length > 4) {
                    Integer c = parseNumber(tokens[4]);
                    if (c != null && c >= 0 && c <= 15) {
                        count = toBinary(c, 4);
                    } else {
//                        System.err.printf("[ASM ERROR] LOC %04o, %s: Count value (%s) out of 4-bit range (0-15).%n", loc, instr, tokens[4]);
                        errorFlag = true;
                    }
                }

                bin = opcode6 + Rr + AL + LR + count + "00";
                bin = String.format("%-16s", bin).replace(' ', '0');

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // TRAP: opcode(6) | 00 | trapVector(8)
            else if (instr.equals("TRAP")) {
                String vector8 = "00000000";

                if (tokens.length > 1) {
                    Integer v = parseNumber(tokens[1]);
                    if (v != null) vector8 = toBinary(v, 8);
                }

                bin = opcode6 + "00" + vector8;
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // IN / OUT / CHK: opcode(6) | R(2) | device(8)
            else if (Set.of("IN","OUT","CHK").contains(instr)) {
                String R = "00";
                String dev8 = "00000000";

                if (tokens.length > 1) {
                    R = regToBin(tokens[1]);
                }
                if (tokens.length > 2) {
                    Integer dev = parseNumber(tokens[2]);
                    if (dev != null) dev8 = toBinary(dev, 8);
                }

                bin = opcode6 + R + dev8;
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // AIR / SIR: opcode(6) | R(2) | Imm(8)
            else if (Set.of("AIR","SIR").contains(instr)) {
                String R = "00";
                String imm8 = "00000000";

                if (tokens.length - 1 != 2) {
//                    System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid operands. Expected (R, Imm). (Found %d operands)%n",
//                            loc, instr, tokens.length - 1);
                    errorFlag = true;
                } else {
                    R = regToBin(tokens[1]);
                    Integer imm = parseNumber(tokens[2]);
                    if (imm != null) {
                        if (imm < -128 || imm > 127) {
//                            System.err.printf("[ASM WARNING] LOC %04o, %s: Immediate value (%d) exceeds 8-bit signed range (-128 to 127). Value will be truncated.%n",
//                                    loc, instr, imm);
                        }
                        imm8 = toBinary(imm, 8);
                    } else {
                        System.err.printf("[ASM ERROR] LOC %04o, %s: Invalid immediate value: %s%n", loc, instr, tokens[2]);
                        errorFlag = true;
                    }
                }

                bin = opcode6 + R + imm8;
                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            // Memory reference instructions
            else if (Set.of("LDR","STR","LDA","JZ","JNE","JCC","JMA","JSR","RFS","JGE",
                    "AMR","SMR","SOB").contains(instr)) {

                int operandLength = tokens.length - 1;

                // Check trailing ",1" (indirect bit)
                boolean ind = (operandLength > 0 && tokens[tokens.length - 1].equals("1"));
                String I = ind ? "1" : "0";

                int requiredOperands = operandLength - (ind ? 1 : 0);

                String R = "00";
                String IX = "00";
                int addr = 0;

                if (Set.of("JMA","JSR","RFS").contains(instr)) {
                    if (requiredOperands == 1) {
                        addr = resolve(tokens[1]);
                    } else if (requiredOperands == 2) {
                        IX = ixToBin(tokens[1]);
                        addr = resolve(tokens[2]);
                    } else {
//                        System.err.printf(
//                                "[ASM ERROR] LOC %04o, %s: Invalid operands. Expected (Address) or (IX, Address). (Found %d operands)%n",
//                                loc, instr, requiredOperands);
                        errorFlag = true;
                    }
                } else {
                    // R-based ops
                    if (requiredOperands == 2) {
                        R = regToBin(tokens[1]);
                        addr = resolve(tokens[2]);
                    } else if (requiredOperands == 3) {
                        R = regToBin(tokens[1]);
                        IX = ixToBin(tokens[2]);
                        addr = resolve(tokens[3]);
                    } else {
//                        System.err.printf(
//                                "[ASM ERROR] LOC %04o, %s: Too few or too many operands. Expected 2 (R, Address) or 3 (R, IX, Address). (Found %d operands)%n",
//                                loc, instr, requiredOperands);
                        errorFlag = true;
                    }
                }

                boolean isImmediate = Set.of("AIR", "SIR").contains(instr);
                boolean isIndexed = !IX.equals("00") || I.equals("1");
                if (addr > 31 && !isImmediate) {
                    if (!isIndexed) {
//                        System.err.printf(
//                                "[ASM ERROR] LOC %04o, %s: Address (%d) exceeds 5-bit limit (31) and no Index Register (IX) is used. Indexing is required.%n",
//                                loc, instr, addr);
                        errorFlag = true;
                    }
                } else if (addr > 31 && isImmediate) {
//                    System.err.printf(
//                            "[ASM ERROR] LOC %04o, %s: Immediate value (%d) exceeds 5-bit limit (31).%n",
//                            loc, instr, addr);
                    errorFlag = true;
                }

                int rVal = Integer.parseInt(R, 2);
                int ixVal = Integer.parseInt(IX, 2);
                int iVal = Integer.parseInt(I, 2);

                int word;
                boolean uses11bit = Set.of("JMA", "JSR", "LDX", "STX").contains(instr);

                if (uses11bit) {
                    word = (opcodeVal << 10)
                            | (rVal << 8)
                            | (ixVal << 6)
                            | (iVal << 5)
                            | (addr & 0x7FF);
                } else {
                    word = (opcodeVal << 10)
                            | (rVal << 8)
                            | (ixVal << 6)
                            | (iVal << 5)
                            | (addr & 0x1F);
                }

                bin = toBinary(word, 16);
//                System.out.printf("[ASM] %s @%04o addr=%04o word=%06o%n", instr, loc, addr, word);

                memory.put(loc, bin);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }
            else if (instr.equalsIgnoreCase("END")) {
                break;
            }
            else {
//                System.err.printf("[ASM ERROR] LOC %04o: Unknown mnemonic: %s%n", loc, instr);
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
//        System.out.println("[DEBUG] Writing listing file to " + outFile);
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
//        System.out.println("[DEBUG] Writing load file to " + outFile);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {

            // Get and sort addresses so output is in ascending order
            List<Integer> addresses = new ArrayList<>(memory.keySet());
            Collections.sort(addresses);

            for (int i = 0; i < addresses.size(); i++) {
                int addr = addresses.get(i);
                String addrOct = toOctal(addr);

                String valBinary = memory.get(addr);
                int valInt = safeParse(valBinary);
                String valOct = toOctal(valInt);

                if (i < addresses.size() - 1) {
                    // All but the last line: include newline
                    writer.printf("%s %s%n", addrOct, valOct);
                } else {
                    // Last line: NO trailing newline
                    writer.printf("%s %s", addrOct, valOct);
                }
            }
        }
    }

    /* ======================== SAFE PARSE ======================== */
    private static int safeParse(String valBinary) {
        try {
            if (valBinary.length() > 16) valBinary = valBinary.substring(valBinary.length() - 16);

            int val;
            if (valBinary.matches("[01]+"))
                val = Integer.parseInt(valBinary, 2);
            else
                val = Integer.parseInt(valBinary);
            return val & 0xFFFF;
        } catch (Exception e) {
            return 0;
        }
    }
}
