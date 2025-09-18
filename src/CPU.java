import java.io.*;
import java.util.*;

public class CPU {
    // ==== Registers ====
    private int[] R = new int[4];     // General purpose registers R0-R3
    private int[] X = new int[4];     // Index registers X1-X3 (X[0] unused)
    private int FR0 = 0;              // Floating register 0
    private int FR1 = 0;              // Floating register 1
    private int PC = 0;               // Program Counter
    private int IR = 0;               // Instruction Register
    private boolean halted = false;

    // ==== Memory (2K words) ====
    private int[] memory = new int[2048];

    // ==== Load program from .ld file ====
    public void loadProgram(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                int addr = Integer.parseInt(parts[0], 8); // octal address
                int val  = Integer.parseInt(parts[1], 8); // octal code
                memory[addr] = val;
            }
        }
    }

    // ==== Run Program ====
    public void run() {
        while (!halted) {
            IR = memory[PC];   // Fetch
            PC++;

            // Decode fields
            int opcode = (IR >>> 10) & 0x3F;   // 6 bits
            int r      = (IR >>> 8) & 0x03;    // 2 bits
            int ix     = (IR >>> 6) & 0x03;    // 2 bits
            int i      = (IR >>> 5) & 0x01;    // 1 bit
            int addr   = IR & 0x1F;            // 5 bits

            int EA = calculateEA(ix, i, addr); // effective address

            executeInstruction(opcode, r, ix, i, EA);
        }
    }

    // ==== Effective Address Calculation ====
    private int calculateEA(int ix, int i, int addr) {
        int ea = addr;
        if (ix > 0) ea += X[ix];   // indexed
        if (i == 1) ea = memory[ea]; // indirect
        return ea & 0x7FF; // 11-bit addresses
    }

    // ==== Instruction Execution ====
    private void executeInstruction(int opcode, int r, int ix, int i, int EA) {
        switch (opcode) {

            // ==== Example basic instructions ====
            case 0x01: // LDR
                R[r] = memory[EA];
                break;

            case 0x02: // STR
                memory[EA] = R[r];
                break;

            case 0x00: // HLT
                halted = true;
                System.out.println("CPU halted.");
                break;

            // ==== Floating Point & Vector Instructions ====
            case 0x22: // FADD
                if (r == 0) FR0 = FloatingPointUnit.addFp16(FR0, memory[EA]);
                else if (r == 1) FR1 = FloatingPointUnit.addFp16(FR1, memory[EA]);
                break;

            case 0x24: // FSUB
                if (r == 0) FR0 = FloatingPointUnit.subFp16(FR0, memory[EA]);
                else if (r == 1) FR1 = FloatingPointUnit.subFp16(FR1, memory[EA]);
                break;

            case 0x25: // VADD
                new VectorUnit(memory).vadd((r == 0) ? FR0 : FR1, EA);
                break;

            case 0x26: // VSUB
                new VectorUnit(memory).vsub((r == 0) ? FR0 : FR1, EA);
                break;

            case 0x27: // CNVRT
                if (r == 0) {
                    FR0 = FloatingPointUnit.intToFp16(memory[EA]);
                } else if (r == 1) {
                    R[ix] = FloatingPointUnit.fp16ToInt(memory[EA]);
                }
                break;

            case 0x28: // LDFR
                if (r == 0) FR0 = memory[EA];
                else if (r == 1) FR1 = memory[EA];
                break;

            case 0x29: // STFR
                if (r == 0) memory[EA] = FR0;
                else if (r == 1) memory[EA] = FR1;
                break;

            default:
                System.err.println("Unknown opcode: " + opcode);
                halted = true;
                break;
        }
    }

    // ==== Debug Memory Dump ====
    public void dumpMemory(int start, int end) {
        for (int i = start; i <= end; i++) {
            System.out.printf("Addr %d: %d%n", i, memory[i]);
        }
    }

    // ==== Main Entry Point ====
    public static void main(String[] args) throws IOException {
        CPU cpu = new CPU();
        cpu.loadProgram("load.ld"); // program produced by Assembler
        cpu.run();

        // Example: print vector A after VADD test
        cpu.dumpMemory(100, 104);
    }
}
