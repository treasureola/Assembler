package Assembler;

import java.io.*;
import java.util.*;

/**
 * Assembler.java
 *
 * A simple two-pass assembler implementation.
 *
 * Overview:
 *   An assembler translates assembly language
 *   into binary machine code
 *
 * This assembler does:
 *   1. Reads an input file called "source.src".
 *   2. Pass 1 → Builds a SYMBOL TABLE:
 *        - Maps labels (like LOOP, START) to memory addresses.
 *   3. Pass 2 → Generates MACHINE CODE:
 *        - Converts each assembly instruction or data definition into
 *          a 16-bit binary representation.
 *   4. Outputs:
 *        - output.lst
 *        - load.ld
 */
public class Assembler {
    /* -----------------------------------------------------------
     * Main Method
     * -----------------------------------------------------------
     * Orchestrates everything:
     *   1. Read source.src program
     *   2. Pass 1 → build symbol table
     *   3. Pass 2 → generate machine code
     *   4. Write output files
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Assembler running...");

        String sourceFile = "source.src";
        File file = new File(sourceFile);

        if (!file.exists()) {
            System.err.println("Error: source.src not found in current directory.");
            return;
        }

        List<String> program = new ArrayList<>();

        // Read the source file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                program.add(line);
            }
        }

        // Run assembler
        pass1(program);
        pass2(program);
        generateListing("output.lst");
        generateLoadFile("load.ld");

        System.out.println("Assembler completed. Output files: output.lst, load.ld");
    }



    /* -----------------------------------------------------------
     * 1) Opcode Table
     * -----------------------------------------------------------
     * Maps assembly mnemonics (like LDR, LDA, STR) to their 6-bit
     * opcode values as binary strings.
     *
     * Machine instruction format (16 bits):
     *   [6 bits: opcode][2 bits: register][2 bits: index register][1 bit: indirect][5 bits: address]
     *
     * Example:  LDR 3,0,10
     *   opcode = "000001" (LDR)
     *   R      = "11"    (register 3)
     *   IX     = "00"    (no index register)
     *   I      = "0"     (direct addressing)
     *   addr   = "01010" (decimal 10 → binary 01010)
     *   Final machine code = 000001110001010
     */
    private static final Map<String, String> opcodeTable = new HashMap<>();
    static {
        // Miscellaneous Instructions
        opcodeTable.put("HLT", "000000"); // Halt program execution
        opcodeTable.put("TRAP", "011000"); // Software interrupt (trap)

        // Load/Store Instructions
        opcodeTable.put("LDR", "000001"); // Load register from memory
        opcodeTable.put("LDA", "000011"); // Load accumulator
        opcodeTable.put("STR", "000010"); // Store register to memory
        opcodeTable.put("LDX", "100001"); // Load index register
        opcodeTable.put("STX", "101010"); // Store index register

        // Transfer (branch/jump) Instructions
        opcodeTable.put("JZ",  "001000"); // Jump if Zero flag is set
        opcodeTable.put("JNE", "001001"); // Jump if Not Equal
        opcodeTable.put("JCC", "001010"); // Jump if Carry Clear
        opcodeTable.put("JMA", "001011"); // Jump unconditional
        opcodeTable.put("JSR", "001100"); // Jump to Subroutine
        opcodeTable.put("RFS", "001101"); // Return from Subroutine
        opcodeTable.put("SOB", "001110"); // Subtract One & Branch
        opcodeTable.put("JGE", "001111"); // Jump if Greater or Equal

        // Arithmetic/Logical Instructions
        opcodeTable.put("AMR", "000100"); // Add memory to register
        opcodeTable.put("SMR", "000101"); // Subtract memory from register
        opcodeTable.put("AIR", "000110"); // Add Immediate to register
        opcodeTable.put("SIR", "000111"); // Subtract Immediate

        // Multiply/Divide Instructions
        opcodeTable.put("MLT", "111000"); // Multiply registers
        opcodeTable.put("DVD", "111001"); // Divide registers
        opcodeTable.put("TRR", "111010"); // Test register equality
        opcodeTable.put("AND", "111011"); // Logical AND
        opcodeTable.put("ORR", "111100"); // Logical OR
        opcodeTable.put("NOT", "111101"); // Logical NOT

        // Shift/Rotate Instructions
        opcodeTable.put("SRC", "011111"); // Shift register
        opcodeTable.put("RRC", "100010"); // Rotate register

        // I/O Instructions
        opcodeTable.put("IN",  "010100"); // Input from device
        opcodeTable.put("OUT", "010101"); // Output to device
        opcodeTable.put("CHK", "110011"); // Check device status

        // Floating-point & Vector Instructions
        opcodeTable.put("FADD",  "100010"); // Floating Add
        opcodeTable.put("FSUB",  "100100"); // Floating Subtract
        opcodeTable.put("VADD",  "100101"); // Vector Add
        opcodeTable.put("VSUB",  "100110"); // Vector Subtract
        opcodeTable.put("CNVRT", "100111"); // Convert integer ↔ float
        opcodeTable.put("LDFR",  "010000"); // Load floating register
        opcodeTable.put("STFR",  "101001"); // Store floating register
    }

    /* -----------------------------------------------------------
     * Data Structures
     * -----------------------------------------------------------
     * - symbolTable : maps labels to their memory addresses
     * - memory      : maps memory locations to generated machine code
     * - sourceMap   : keeps the original source.src line for listing file
     * - locMap      : stores LOC pseudo-op references
     */
    private static final Map<String, Integer> symbolTable = new HashMap<>();
    private static final Map<Integer, String> memory = new TreeMap<>();
    private static final Map<Integer, String> sourceMap = new TreeMap<>();
    private static final Map<Integer, String> locMap = new TreeMap<>();

    /* -----------------------------------------------------------
     * Helper Functions
     * -----------------------------------------------------------
     */

    // Convert register operand (e.g., "R1") into 2-bit binary string
    private static String regToBin(String r) {
        String digits = r.replaceAll("[^0-9\\-]", ""); // keep only digits
        if (digits.isEmpty()) digits = "0";            // default = 0
        int num = Integer.parseInt(digits);
        return toBinary(num, 2);
    }

    // Convert index register operand (e.g., "X2") into 2-bit binary
    private static String ixToBin(String ix) {
        String digits = ix.replaceAll("[^0-9\\-]", "");
        if (digits.isEmpty()) digits = "0";
        int num = Integer.parseInt(digits);
        return toBinary(num, 2);
    }

    // Convert integer into fixed-length binary string
    private static String toBinary(int value, int bits) {
        int mask = (1 << bits) - 1;     // e.g., 2 bits → mask = 11b
        int v = value & mask;           // ensure value fits within 'bits'
        String bin = Integer.toBinaryString(v);
        if (bin.length() > bits) bin = bin.substring(bin.length() - bits);
        return String.format("%" + bits + "s", bin).replace(' ', '0');
    }

    // Convert integer to 6-digit octal (used in output files)
    private static String toOctal(int value) {
        int val = value & 0xFFFF;  // ensure 16-bit unsigned range
        return String.format("%06o", val);
    }

    // Parse number (decimal or hex "0x...") into integer
    private static Integer parseNumber(String s) {
        s = s.trim();
        try {
            if (s.startsWith("0x") || s.startsWith("0X")) {
                return Integer.parseInt(s.substring(2), 16);
            } else {
                return Integer.parseInt(s);
            }
        } catch (NumberFormatException nfe) {
            return null; // means operand might be a label
        }
    }

    /* -----------------------------------------------------------
     * Pass 1: Build Symbol Table
     * -----------------------------------------------------------
     * - Reads each line
     * - Tracks current memory location (LOC)
     * - Adds labels to the symbol table with their memory address
     */
    public static void pass1(List<String> lines) {
        int loc = 0;
        for (String rawLine : lines) {
            String line = rawLine;

            // Remove comments (everything after ';')
            int sem = line.indexOf(';');
            if (sem >= 0) line = line.substring(0, sem);
            line = line.trim();
            if (line.isEmpty()) continue; // skip blank lines

            // Extract label (if any)
            String label = null;
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                label = parts[0].trim().toUpperCase();
                line  = parts.length > 1 ? parts[1].trim() : "";
            }

            // Process instruction or data
            if (!line.isEmpty()) {
                String[] tokens = line.split("\\s+");
                String instr = tokens[0].toUpperCase();

                if (instr.equals("LOC")) {
                    // Change memory location counter
                    if (tokens.length > 1) {
                        loc = Integer.parseInt(tokens[1]);
                        if (label != null) symbolTable.put(label, loc);
                    }
                } else if (instr.equals("DATA")) {
                    // Reserve memory for data
                    if (label != null) symbolTable.put(label, loc);
                    loc++;
                } else {
                    // Normal instruction
                    if (label != null) symbolTable.put(label, loc);
                    loc++;
                }
            } else {
                // Line with only a label
                if (label != null) symbolTable.put(label, loc);
            }
        }
    }

    /* -----------------------------------------------------------
     * Pass 2: Generate Machine Code
     * -----------------------------------------------------------
     * - Converts mnemonics into binary instructions
     * - Replaces labels with actual addresses (using symbolTable)
     */
    public static void pass2(List<String> lines) {
        int loc = 0;
        for (String rawLine : lines) {
            String original = rawLine;
            String line = rawLine;

            // Strip comments
            int sem = line.indexOf(';');
            if (sem >= 0) line = line.substring(0, sem);
            line = line.trim();
            if (line.isEmpty()) continue;

            // Remove label if present
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                line = parts.length > 1 ? parts[1].trim() : "";
                if (line.isEmpty()) continue;
            }

            String[] tokens = line.split("[,\\s]+");
            String instr = tokens[0].toUpperCase();

            // LOC directive
            if (instr.equals("LOC")) {
                loc = Integer.parseInt(tokens[1]);
                locMap.put(loc, original);
                continue;
            }

            // DATA directive
            if (instr.equals("DATA")) {
                String val = tokens[1];
                Integer num = parseNumber(val);
                if (num == null) num = symbolTable.getOrDefault(val.toUpperCase(), 0);
                memory.put(loc, toBinary(num, 16));
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            // Get opcode
            String opcode = opcodeTable.getOrDefault(instr, "000000");
            String R = "00", IX = "00", I = "0";
            int addr = 0;

            // Special case: HLT
            if (instr.equals("HLT")) {
                memory.put(loc, opcode + "0000000000");
                sourceMap.put(loc, original);
                loc++;
                continue;
            }

            // Special case: LDX
            if (instr.equals("LDX") && tokens.length >= 3) {
                R = "00";
                IX = toBinary(Integer.parseInt(tokens[1]), 2);  // convert "1" → 01 (binary)
                String operand = tokens[2];
                addr = parseNumber(operand) != null ? parseNumber(operand) : symbolTable.getOrDefault(operand.toUpperCase(), 0);

                if (tokens.length > 3) {
                    I = (tokens[3].equals("1") || tokens[3].equalsIgnoreCase("I")) ? "1" : "0";
                }

                String address = toBinary(addr, 5);
                String machineCode = opcode + R + IX + I + address;

                memory.put(loc, machineCode);
                sourceMap.put(loc, original);
                loc++;
                continue;
            }


            // General instruction case
            if (tokens.length > 1) R = regToBin(tokens[1]);
            if (tokens.length > 2) IX = ixToBin(tokens[2]);
            if (tokens.length > 3) {
                String operand = tokens[3];
                Integer num = parseNumber(operand);
                addr = (num != null) ? num : symbolTable.getOrDefault(operand.toUpperCase(), 0);
            }
            if (tokens.length > 4) {
                I = (tokens[4].equals("1") || tokens[4].equalsIgnoreCase("I")) ? "1" : "0";
            }

            String address = toBinary(addr, 5);
            String machineCode = opcode + R + IX + I + address;

            memory.put(loc, machineCode);
            sourceMap.put(loc, original);
            loc++;
        }
    }


    /* -----------------------------------------------------------
     * Generate Listing File
     * -----------------------------------------------------------
     * Format:
     *   <address in octal> <machine code in octal> <original source.src>
     */
    public static void generateListing(String outFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
            Set<Integer> allAddrs = new TreeSet<>();
            allAddrs.addAll(memory.keySet());
            allAddrs.addAll(locMap.keySet());

            for (int addr : allAddrs) {
                if (locMap.containsKey(addr)) {
                    // Print LOC pseudo-op
                    writer.printf("              %s%n", locMap.get(addr));
                    if (memory.containsKey(addr)) {
                        String addrOct = toOctal(addr);
                        String valOct  = toOctal(Integer.parseInt(memory.get(addr), 2));
                        writer.printf("%s %s %s%n", addrOct, valOct, sourceMap.get(addr));
                    }
                } else if (memory.containsKey(addr)) {
                    String addrOct = toOctal(addr);
                    String valOct  = toOctal(Integer.parseInt(memory.get(addr), 2));
                    writer.printf("%s %s %s%n", addrOct, valOct, sourceMap.get(addr));
                }
            }
        }
    }

    /* -----------------------------------------------------------
     * Generate Load File
     * -----------------------------------------------------------
     * Format:
     *   <address in octal> <machine code in octal>
     */
    public static void generateLoadFile(String outFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
            List<Integer> addresses = new ArrayList<>(memory.keySet());

            for (int i = 0; i < addresses.size(); i++) {
                int addr = addresses.get(i);
                String addrOct = toOctal(addr);
                String valOct = toOctal(Integer.parseInt(memory.get(addr), 2));

                if (i < addresses.size() - 1) {
                    writer.printf("%s %s%n", addrOct, valOct);  // with newline
                } else {
                    writer.printf("%s %s", addrOct, valOct);    // no trailing newline
                }
            }
        }
    }

}