package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;

public class MachineCore {
    private final Memory memory;
    private final Registers regs;
    private boolean halted = false;

    public MachineCore(Memory memory, Registers regs) {
        this.memory = memory;
        this.regs = regs;
    }

    // ===============================================================
    // SYSTEM CONTROL
    // ===============================================================
    public void reset() {
        halted = false;
        memory.reset();
        regs.reset();
    }

    public boolean isHalted() {
        return halted;
    }

    public Registers getRegisters() {
        return regs;
    }

    public Memory getMemory() {
        return memory;
    }

    // ===============================================================
    // FETCH-DECODE-EXECUTE CYCLE
    // ===============================================================
    public void step() {
        if (halted) return;

        int pc = regs.getPC();
        regs.setMAR(pc);
        int word = memory.readWord(pc);
        regs.setMBR(word);
        regs.setIR(word);
        regs.setPC(pc + 1); // move to next instruction

        int opcode = (word >> 10) & 0x3F; // 6-bit opcode field

        if (opcode == 0) {
            halted = true;
            System.out.printf("[HLT] Halting at PC=%06o%n", pc);
            return;
        }

        execute(word);
    }

    public void run() {
        while (!halted) {
            step();
        }
    }

    // ===============================================================
    // EXECUTION UNIT
    // ===============================================================
    private void execute(int instr) {
        int opcode = (instr >> 10) & 0x3F;  // top 6 bits (bits 15–10)
        int r = (instr >> 8) & 0x03;        // bits 9–8
        int ix = (instr >> 6) & 0x03;       // bits 7–6
        int i = (instr >> 5) & 0x01;        // bit 5 (indirect)
        int addr = instr & 0x1F;            // bits 4–0 (5-bit address)

        int ea = computeEA(ix, i, addr);

        switch (opcode) {

            // ---------------------------
            // Load / Store
            // ---------------------------
            case 1: // LDR
                loadRegister(r, ea);
                break;

            case 2: // STR
                storeRegister(r, ea);
                break;

            case 3: // LDA
                regs.setGPR(r, ea);
                System.out.printf("[LDA] R%d ← EA=%06o%n", r, ea);
                break;

            // ---------------------------
            // Index Register
            // ---------------------------
            case 41: // LDX (octal 101001)
                loadIndex(ix, ea);
                break;

            case 42: // STX
                storeIndex(ix, ea);
                break;

            // ---------------------------
            // Branching
            // ---------------------------
            case 20: // JZ (octal 020)
                if (regs.getGPR(r) == 0) {
                    regs.setPC(ea);
                    System.out.printf("[JZ] R%d==0 → PC←%06o%n", r, ea);
                }
                break;

            // ---------------------------
            // HALT
            // ---------------------------
            case 0: // HLT
                halted = true;
                System.out.println("[HLT] Program halted.");
                break;

            default:
                System.out.printf("[WARN] Unknown opcode %02o at PC=%06o%n", opcode, regs.getPC());
        }
    }

    // ===============================================================
    // HELPER METHODS
    // ===============================================================
    private int computeEA(int ix, int i, int addr) {
        int ea = addr;
        if (ix > 0) ea += regs.getIX(ix);
        if (i == 1) ea = memory.readWord(ea);
        return ea & 0x7FF; // limit to 11-bit address space
    }

    private void loadRegister(int r, int ea) {
        int val = memory.readWord(ea);
        regs.setGPR(r, val);
        System.out.printf("[LDR] R%d ← MEM[%06o] = %06o%n", r, ea, val);
    }

    private void storeRegister(int r, int ea) {
        int val = regs.getGPR(r);
        memory.writeWord(ea, val);
        System.out.printf("[STR] MEM[%06o] ← R%d = %06o%n", ea, r, val);
    }

    private void loadIndex(int ix, int ea) {
        int val = memory.readWord(ea);
        regs.setIX(ix, val);
        System.out.printf("[LDX] X%d ← MEM[%06o] = %06o%n", ix, ea, val);
    }

    private void storeIndex(int ix, int ea) {
        int val = regs.getIX(ix);
        memory.writeWord(ea, val);
        System.out.printf("[STX] MEM[%06o] ← X%d = %06o%n", ea, ix, val);
    }
}
