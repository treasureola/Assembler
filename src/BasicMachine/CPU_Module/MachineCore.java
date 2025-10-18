package BasicMachine.CPU_Module;

import BasicMachine.Memory_Module.Memory;

/**
 * MachineCore.java
 * -----------------
 * Implements the CPU execution cycle for the Basic Machine.
 *
 * Integration:
 *  - Reads/writes through Memory_Module.
 *  - Provides public methods for Testing_Module and UI_Module.
 */
public class MachineCore {
    private final Memory memory;
    private final Registers regs;
    private boolean halted = false;

    public MachineCore(Memory mem, Registers regs) {
        this.memory = mem;
        this.regs = regs;
    }

    /** Run continuously until HLT. */
    public void run() {
        while (!halted) runOneCycle();
    }

    /** Execute one fetch–decode–execute cycle. */
    public void runOneCycle() {
        fetch();
        decodeAndExecute();
    }

    /** FETCH PHASE:
     *  MAR ← PC
     *  MBR ← Mem[MAR]
     *  IR  ← MBR
     *  PC  ← PC + 1
     */
    private void fetch() {
        int address = regs.PC.get();
        regs.MAR.set(address);
        regs.MBR.set(memory.readWord(address));
        regs.IR.set(regs.MBR.get());
        regs.PC.set(address + 1);
    }

    /** Decode IR fields and execute corresponding instruction. */
    private void decodeAndExecute() {
        int instr = regs.IR.get();

        // bit-field extraction
        int opcode  = (instr >> 10) & 0x3F;
        int r       = (instr >> 8) & 0x03;
        int ix      = (instr >> 6) & 0x03;
        int i       = (instr >> 5) & 0x01;
        int address =  instr & 0x1F;

        switch (opcode) {
            case 1 -> executeLDR(r, ix, i, address);
            case 2 -> executeSTR(r, ix, i, address);
            case 33 -> executeHLT();
            default -> {
                System.out.printf("Unknown opcode %d, halting.%n", opcode);
                halted = true;
            }
        }

        printState();  // debug output for Testing/UI
    }

    /** Compute effective address from IX and I bits. */
    private int calcEA(int ix, int i, int addr) {
        int ea = addr;
        if (ix > 0) ea += regs.IXR[ix - 1].get(); // indexed
        if (i == 1) ea = memory.readWord(ea);         // indirect
        return ea;
    }

    /** LDR r,ix,i,addr — Load register from memory. */
    private void executeLDR(int r, int ix, int i, int addr) {
        int ea = calcEA(ix, i, addr);
        int val = memory.readWord(ea);
        regs.GPR[r].set(val);
    }

    /** STR r,ix,i,addr — Store register into memory. */
    private void executeSTR(int r, int ix, int i, int addr) {
        int ea = calcEA(ix, i, addr);
        memory.writeWord(ea, regs.GPR[r].get());
    }

    private void executeHLT() {
        halted = true;
        System.out.println("Program halted.");
        printState(); // show final octal state
    }


    /** Print CPU register state in octal format for debugging/UI. */
    public void printState() {
        System.out.printf(
                "PC=%04o  IR=%06o  MAR=%04o  MBR=%06o%n",
                regs.PC.get(), regs.IR.get(),
                regs.MAR.get(), regs.MBR.get()
        );

        System.out.printf(
                "R0=%06o  R1=%06o  R2=%06o  R3=%06o%n",
                regs.GPR[0].get(), regs.GPR[1].get(),
                regs.GPR[2].get(), regs.GPR[3].get()
        );

        System.out.printf(
                "X1=%06o  X2=%06o  X3=%06o%n%n",
                regs.IXR[0].get(), regs.IXR[1].get(), regs.IXR[2].get()
        );
    }


    public boolean isHalted() { return halted; }
}
